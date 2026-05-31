package com.compliancemind.soc.service.commerce;

import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.dto.commerce.PaymentStatusResponse;
import com.compliancemind.soc.dto.commerce.PaymentSubmitRequest;
import com.compliancemind.soc.dto.commerce.PaymentSubmitResponse;
import com.compliancemind.soc.entity.commerce.OrderRecord;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 支付：提交订单支付、查询状态、回调处理、模拟成功（联调）。
 */
@Service
public class PaymentService {

    private final OrderService orderService;

    public PaymentService(OrderService orderService) {
        this.orderService = orderService;
    }

    public PaymentSubmitResponse submit(PaymentSubmitRequest request) {
        OrderRecord orderRecord = orderService.createOrder(request);
        PaymentSubmitResponse response = new PaymentSubmitResponse();
        response.setOrderNo(orderRecord.getOrderNo());
        response.setPaymentUrl(SocConstants.PaymentCallback.MOCK_SUCCESS_URL_PREFIX + "?" + SocConstants.PaymentCallback.PARAM_ORDER_NO + "=" + urlEncode(orderRecord.getOrderNo()));
        response.setQrCode("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNkYPhfDwAChwGA60e6kgAAAABJRU5ErkJggg==");
        response.setExpireTime(LocalDateTime.now().plusMinutes(SocConstants.PaymentCallback.MOCK_EXPIRE_MINUTES).format(DateTimeFormatter.ofPattern(SocConstants.Format.DATETIME_SECONDS)));
        response.setAmount(orderRecord.getAmount());
        response.setAnnualPrice(orderRecord.getAmount());
        response.setAuditType(orderRecord.getAuditType());
        return response;
    }

    public PaymentStatusResponse query(String orderNo) {
        return orderService.paymentStatus(orderNo);
    }

    public void handleNotify(String orderNo, String status, String transactionId) {
        if (SocConstants.Order.NOTIFY_STATUS_SUCCESS.equalsIgnoreCase(status) || SocConstants.Order.STATUS_PAID.equalsIgnoreCase(status)) {
            orderService.markPaid(orderNo, transactionId == null || transactionId.isBlank()
                ? SocConstants.Order.MOCK_TX_PREFIX + System.currentTimeMillis()
                : transactionId);
            return;
        }
        orderService.markFailed(orderNo, transactionId);
    }

    public String mockSuccess(String orderNo, String returnUrl) {
        orderService.markPaid(orderNo, SocConstants.Order.MOCK_TX_PREFIX + System.currentTimeMillis());
        String target = returnUrl == null || returnUrl.isBlank()
            ? SocConstants.PaymentCallback.ORDER_DETAIL_URL_PREFIX + orderNo
            : returnUrl;
        if (target.contains("?")) {
            return target + "&" + SocConstants.PaymentCallback.PARAM_ORDER_NO + "=" + urlEncode(orderNo)
                + "&" + SocConstants.PaymentCallback.PARAM_PAY_STATUS + "=" + SocConstants.PaymentCallback.PAY_STATUS_SUCCESS;
        }
        return target + "?" + SocConstants.PaymentCallback.PARAM_ORDER_NO + "=" + urlEncode(orderNo)
            + "&" + SocConstants.PaymentCallback.PARAM_PAY_STATUS + "=" + SocConstants.PaymentCallback.PAY_STATUS_SUCCESS;
    }

    private String urlEncode(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8);
    }
}
