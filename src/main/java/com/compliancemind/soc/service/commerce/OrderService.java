package com.compliancemind.soc.service.commerce;

import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.dto.commerce.PaymentStatusResponse;
import com.compliancemind.soc.dto.commerce.PaymentSubmitRequest;
import com.compliancemind.soc.entity.commerce.OrderRecord;
import com.compliancemind.soc.entity.commerce.Product;
import com.compliancemind.soc.entity.commerce.ProductPackage;
import com.compliancemind.soc.entity.commerce.UserProduct;
import com.compliancemind.soc.mapper.commerce.OrderMapper;
import com.compliancemind.soc.mapper.commerce.ProductMapper;
import com.compliancemind.soc.mapper.commerce.UserProductMapper;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单：创建、我的订单、详情、取消（仅未支付单等规则在方法内约束）。
 */
@Service
public class OrderService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(SocConstants.Format.DATETIME_SECONDS);

    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final UserProductMapper userProductMapper;
    private final UserAccountMapper userAccountMapper;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;

    public OrderService(OrderMapper orderMapper,
                        ProductMapper productMapper,
                        UserProductMapper userProductMapper,
                        UserAccountMapper userAccountMapper,
                        CurrentUserAccessor currentUserAccessor,
                        OperationLogService operationLogService) {
        this.orderMapper = orderMapper;
        this.productMapper = productMapper;
        this.userProductMapper = userProductMapper;
        this.userAccountMapper = userAccountMapper;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
    }

    @Transactional(rollbackFor = Exception.class)
    public OrderRecord createOrder(PaymentSubmitRequest request) {
        UserAccount currentUser = currentUser();
        Product product = productMapper.selectById(request.getProductId());
        if (product == null) {
            throw new BizException(BizErrorCode.COMMERCE_PRODUCT_NOT_FOUND);
        }
        ProductPackage productPackage = productMapper.selectPackageById(request.getPackageId());
        if (productPackage == null || !product.getProductId().equals(productPackage.getProductId())) {
            throw new BizException(BizErrorCode.COMMERCE_PACKAGE_NOT_FOUND);
        }
        OrderRecord existed = request.getOrderNo() == null || request.getOrderNo().isBlank()
            ? null
            : orderMapper.selectByOrderNo(request.getOrderNo());
        if (existed != null && currentUser.getUserId().equals(existed.getUserId())) {
            return existed;
        }

        String resolvedAuditType = resolveAuditType(request.getAuditType(), productPackage.getDefaultType());
        Integer resolvedAmount = resolveAmount(productPackage, resolvedAuditType);
        if (resolvedAmount <= 0) {
            throw new BizException(BizErrorCode.ORDER_INVALID_PACKAGE_PRICE);
        }

        OrderRecord orderRecord = new OrderRecord();
        orderRecord.setOrderNo(request.getOrderNo() == null || request.getOrderNo().isBlank()
            ? generateOrderNo(currentUser.getUserId())
            : request.getOrderNo().trim());
        orderRecord.setUserId(currentUser.getUserId());
        orderRecord.setProductId(product.getProductId());
        orderRecord.setPackageId(productPackage.getPackageId());
        orderRecord.setProductName(product.getProductName());
        orderRecord.setPackageName(productPackage.getPackageName());
        orderRecord.setAuditType(SocConstants.AuditType.INTERNAL_TYPE2.equals(resolvedAuditType)
            ? SocConstants.AuditType.DISPLAY_TYPE2
            : SocConstants.AuditType.DISPLAY_TYPE1);
        // Backend is the pricing authority; ignore client amount to prevent tampering.
        orderRecord.setAmount(resolvedAmount);
        orderRecord.setPaymentMethod(request.getPaymentMethod() == null || request.getPaymentMethod().isBlank()
            ? SocConstants.Order.PAYMENT_METHOD_MOCK
            : request.getPaymentMethod().trim());
        orderRecord.setStatus(SocConstants.Order.STATUS_PENDING);
        orderRecord.setReturnUrl(request.getReturnUrl());
        orderRecord.setNotifyUrl(request.getNotifyUrl());
        orderMapper.insert(orderRecord);
        operationLogService.record(SocConstants.OperationLog.Module.ORDER,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.ORDER,
            orderRecord.getOrderNo(),
            product.getProductName(),
            null,
            SocConstants.OperationLog.Detail.ORDER_CREATE_EN);
        return orderRecord;
    }

    public List<OrderRecord> myOrders() {
        UserAccount currentUser = currentUser();
        return orderMapper.listAllByUserId(currentUser.getUserId());
    }

    public OrderRecord detail(String orderNo) {
        return requireOwnedOrder(orderNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancel(String orderNo) {
        OrderRecord orderRecord = requireOwnedOrder(orderNo);
        if (orderMapper.cancel(currentUserAccessor.requireUserId(), orderNo) <= 0) {
            throw new BizException(BizErrorCode.ORDER_CANCEL_INVALID_STATE);
        }
        operationLogService.record(SocConstants.OperationLog.Module.ORDER,
            SocConstants.OperationLog.Action.CANCEL,
            SocConstants.OperationLog.EntityType.ORDER,
            orderNo,
            orderRecord.getProductName(),
            null,
            SocConstants.OperationLog.Detail.ORDER_CANCEL_EN);
    }

    public PaymentStatusResponse paymentStatus(String orderNo) {
        OrderRecord orderRecord = orderMapper.selectByOrderNo(orderNo);
        if (orderRecord == null) {
            throw new BizException(BizErrorCode.ORDER_NOT_FOUND);
        }
        Integer currentUserId = currentUserAccessor.currentUserId();
        if (currentUserId != null && !currentUserId.equals(orderRecord.getUserId())) {
            throw new BizException(BizErrorCode.ORDER_VIEW_DENIED);
        }
        PaymentStatusResponse response = new PaymentStatusResponse();
        response.setOrderNo(orderRecord.getOrderNo());
        response.setStatus(orderRecord.getStatus());
        response.setAmount(orderRecord.getAmount());
        response.setTransactionId(orderRecord.getTransactionId());
        response.setPayTime(orderRecord.getPayTime() == null ? null : orderRecord.getPayTime().format(DATE_TIME_FORMATTER));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markPaid(String orderNo, String transactionId) {
        OrderRecord orderRecord = orderMapper.selectByOrderNo(orderNo);
        if (orderRecord == null) {
            throw new BizException(BizErrorCode.ORDER_NOT_FOUND);
        }
        orderMapper.updatePayment(orderNo, SocConstants.Order.STATUS_PAID, transactionId, LocalDateTime.now());
        OrderRecord paid = orderMapper.selectByOrderNo(orderNo);
        upsertUserProduct(paid);
        recordPaymentLog(SocConstants.OperationLog.Module.PAYMENT,
            SocConstants.OperationLog.Action.PAID,
            orderNo,
            paid.getProductName(),
            SocConstants.OperationLog.Detail.PAYMENT_SUCCESS_EN);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markFailed(String orderNo, String transactionId) {
        OrderRecord orderRecord = orderMapper.selectByOrderNo(orderNo);
        if (orderRecord == null) {
            throw new BizException(BizErrorCode.ORDER_NOT_FOUND);
        }
        orderMapper.updatePayment(orderNo, SocConstants.Order.STATUS_FAILED, transactionId, null);
        recordPaymentLog(SocConstants.OperationLog.Module.PAYMENT,
            SocConstants.OperationLog.Action.FAILED,
            orderNo,
            orderRecord.getProductName(),
            SocConstants.OperationLog.Detail.PAYMENT_FAILED_EN);
    }

    public long countActiveProducts(Integer userId) {
        return userProductMapper.countActiveByUserId(userId);
    }

    private void upsertUserProduct(OrderRecord orderRecord) {
        UserProduct existed = userProductMapper.selectOne(orderRecord.getUserId(), orderRecord.getProductId(), orderRecord.getAuditType());
        if (existed == null) {
            UserProduct userProduct = new UserProduct();
            userProduct.setUserId(orderRecord.getUserId());
            userProduct.setProductId(orderRecord.getProductId());
            userProduct.setProductName(orderRecord.getProductName());
            userProduct.setPackageId(orderRecord.getPackageId());
            userProduct.setPackageName(orderRecord.getPackageName());
            userProduct.setAuditType(orderRecord.getAuditType());
            userProduct.setSourceOrderNo(orderRecord.getOrderNo());
            userProduct.setStatus(SocConstants.UserProduct.STATUS_ACTIVE);
            userProduct.setStartTime(LocalDateTime.now());
            userProductMapper.insert(userProduct);
            return;
        }
        existed.setPackageId(orderRecord.getPackageId());
        existed.setPackageName(orderRecord.getPackageName());
        existed.setSourceOrderNo(orderRecord.getOrderNo());
        existed.setStatus(SocConstants.UserProduct.STATUS_ACTIVE);
        existed.setStartTime(LocalDateTime.now());
        userProductMapper.update(existed);
    }

    private OrderRecord requireOwnedOrder(String orderNo) {
        OrderRecord orderRecord = orderMapper.selectByOrderNo(orderNo);
        if (orderRecord == null || !currentUserAccessor.requireUserId().equals(orderRecord.getUserId())) {
            throw new BizException(BizErrorCode.ORDER_NOT_FOUND);
        }
        return orderRecord;
    }

    private UserAccount currentUser() {
        UserAccount userAccount = userAccountMapper.selectById(currentUserAccessor.requireUserId());
        if (userAccount == null) {
            throw new BizException(BizErrorCode.AUTH_CURRENT_USER_NOT_FOUND);
        }
        return userAccount;
    }

    private String resolveAuditType(String requestAuditType, String packageDefaultType) {
        String normalized = normalizeAuditType(requestAuditType);
        if (normalized != null) {
            return normalized;
        }
        normalized = normalizeAuditType(packageDefaultType);
        return normalized == null ? SocConstants.AuditType.INTERNAL_TYPE1 : normalized;
    }

    private String normalizeAuditType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (SocConstants.AuditType.INTERNAL_TYPE2.equals(normalized)) {
            return SocConstants.AuditType.INTERNAL_TYPE2;
        }
        if (SocConstants.AuditType.INTERNAL_TYPE1.equals(normalized)) {
            return SocConstants.AuditType.INTERNAL_TYPE1;
        }
        return null;
    }

    private Integer resolveAmount(ProductPackage productPackage, String auditType) {
        Integer type1Price = firstPositive(productPackage.getType1Price(), productPackage.getAnnualPrice());
        Integer type2Price = firstPositive(productPackage.getType2Price(), productPackage.getAnnualPrice());
        if (SocConstants.AuditType.INTERNAL_TYPE2.equals(auditType)) {
            return type2Price;
        }
        return type1Price;
    }

    private Integer firstPositive(Integer first, Integer fallback) {
        if (first != null && first > 0) {
            return first;
        }
        if (fallback != null && fallback > 0) {
            return fallback;
        }
        return 0;
    }

    private String generateOrderNo(Integer userId) {
        return SocConstants.Order.ORDER_NO_PREFIX + userId + System.currentTimeMillis()
            + ThreadLocalRandom.current().nextInt(SocConstants.Order.ORDER_NO_RANDOM_ORIGIN, SocConstants.Order.ORDER_NO_RANDOM_BOUND);
    }

    private void recordPaymentLog(String moduleName,
                                  String actionType,
                                  String orderNo,
                                  String productName,
                                  String actionDetail) {
        if (currentUserAccessor.currentUserId() != null) {
            operationLogService.record(moduleName, actionType, SocConstants.OperationLog.EntityType.ORDER, orderNo, productName, null, actionDetail);
            return;
        }
        operationLogService.recordSystem(moduleName, actionType, SocConstants.OperationLog.EntityType.ORDER, orderNo, productName, null, actionDetail);
    }
}
