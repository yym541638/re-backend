package com.compliancemind.soc.service.commerce;

import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.dto.commerce.*;
import com.compliancemind.soc.entity.commerce.Product;
import com.compliancemind.soc.entity.commerce.ProductPackage;
import com.compliancemind.soc.entity.commerce.UserProduct;
import com.compliancemind.soc.mapper.commerce.ProductMapper;
import com.compliancemind.soc.mapper.commerce.UserProductMapper;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * 商品目录、套餐定价、详情 DTO 组装、当前用户订阅列表、管理员改价。
 */
@Service
public class ProductService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(SocConstants.Format.DATETIME_SECONDS);

    private final ProductMapper productMapper;
    private final UserProductMapper userProductMapper;
    private final UserAccountMapper userAccountMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final ObjectMapper objectMapper;

    public ProductService(ProductMapper productMapper,
                          UserProductMapper userProductMapper,
                          UserAccountMapper userAccountMapper,
                          AuthorizationService authorizationService,
                          CurrentUserAccessor currentUserAccessor,
                          ObjectMapper objectMapper) {
        this.productMapper = productMapper;
        this.userProductMapper = userProductMapper;
        this.userAccountMapper = userAccountMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.objectMapper = objectMapper;
    }

    public List<ProductListItem> list() {
        List<ProductListItem> result = new ArrayList<>();
        for (Product product : productMapper.listActiveProducts()) {
            ProductListItem item = new ProductListItem();
            item.setProductId(product.getProductId());
            item.setProductName(product.getProductName());
            item.setProductCode(product.getProductCode());
            item.setIntroductionText(product.getIntroductionText());
            item.setLogoUrl(product.getLogoUrl());
            item.setTrustPrinciples(parseJsonList(product.getTrustPrinciples()));
            result.add(item);
        }
        return result;
    }

    /**
     * 新用户购买页：返回 SOC 2 下全部在售套餐及动态价格，无需传 productId。
     */
    public List<ProductDetail2Response> listSoc2Packages(String selectedAuditType) {
        Product product = productMapper.selectByProductCode(SocConstants.Product.CODE_SOC2);
        if (product == null) {
            throw new BizException(BizErrorCode.COMMERCE_PRODUCT_NOT_FOUND);
        }
        return buildPackageDetailResponses(product, selectedAuditType);
    }

    public List<ProductDetail2Response> detail(Integer productId, String selectedAuditType) {
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(BizErrorCode.COMMERCE_PRODUCT_NOT_FOUND);
        }
        return buildPackageDetailResponses(product, selectedAuditType);
    }

    private List<ProductDetail2Response> buildPackageDetailResponses(Product product, String selectedAuditType) {
        List<ProductDetail2Response> result = new ArrayList<>();
        List<ProductPackage> packageEntities = productMapper.listPackagesByProductId(product.getProductId());
        for (ProductPackage productPackage : packageEntities) {
            ProductDetail2Response response = new ProductDetail2Response();
            response.setProductId(productPackage.getPackageId());
            response.setProductName(productPackage.getPackageName());
            response.setProductCode(product.getProductCode());
            response.setFeatures(parseJsonFeatureList(product.getAllFeatures()));
            response.setTypeSwitch(SocConstants.AuditType.INTERNAL_TYPE2.equals(
                resolveAuditType(selectedAuditType, productPackage.getDefaultType())));
            response.setPrice(resolvePrice(productPackage, selectedAuditType).toString());
            result.add(response);
        }
        return result;
    }

    public ProductPackageItem updatePackagePrice(Integer packageId, Integer type1Price, Integer type2Price, String defaultType) {
        authorizationService.requireCompanyManagement();
        ProductPackage productPackage = productMapper.selectPackageById(packageId);
        if (productPackage == null) {
            throw new BizException(BizErrorCode.COMMERCE_PACKAGE_NOT_FOUND);
        }
        Integer resolvedType1Price = firstPositive(type1Price, productPackage.getType1Price());
        Integer resolvedType2Price = firstPositive(type2Price, productPackage.getType2Price());
        if (resolvedType1Price <= 0 || resolvedType2Price <= 0) {
            throw new BizException(BizErrorCode.COMMERCE_PRICE_INVALID);
        }
        String resolvedDefaultType = resolveAuditType(defaultType, productPackage.getDefaultType());
        String persistedDefaultType = SocConstants.AuditType.INTERNAL_TYPE2.equals(resolvedDefaultType)
            ? SocConstants.AuditType.DISPLAY_TYPE2
            : SocConstants.AuditType.DISPLAY_TYPE1;
        productMapper.updatePackagePrice(packageId, resolvedType1Price, resolvedType2Price, persistedDefaultType);
        ProductPackage latest = productMapper.selectPackageById(packageId);
        ProductPackageItem item = new ProductPackageItem();
        item.setPackageId(latest.getPackageId());
        item.setPackageName(latest.getPackageName());
        item.setAnnualPrice(resolvePrice(latest, resolvedDefaultType));
        item.setType1Price(firstPositive(latest.getType1Price(), latest.getAnnualPrice()));
        item.setType2Price(firstPositive(latest.getType2Price(), latest.getAnnualPrice()));
        item.setIncludedFeatures(parseJsonList(latest.getIncludedFeatures()));
        item.setSupportedTypes(parseJsonList(latest.getSupportedTypes()));
        item.setDefaultType(latest.getDefaultType());
        return item;
    }

    public List<UserProductItem> myProducts() {
        ensureCurrentUser();
        List<UserProductItem> result = new ArrayList<>();
        for (UserProduct userProduct : userProductMapper.listByUserId(currentUserAccessor.requireUserId())) {
            UserProductItem item = new UserProductItem();
            item.setProductId(userProduct.getProductId());
            item.setProductName(userProduct.getProductName());
            item.setPackageId(userProduct.getPackageId());
            item.setPackageName(userProduct.getPackageName());
            item.setAuditType(userProduct.getAuditType());
            item.setStatus(userProduct.getStatus());
            item.setSourceOrderNo(userProduct.getSourceOrderNo());
            item.setStartTime(userProduct.getStartTime() == null ? null : userProduct.getStartTime().format(DATE_TIME_FORMATTER));
            item.setEndTime(userProduct.getEndTime() == null ? null : userProduct.getEndTime().format(DATE_TIME_FORMATTER));
            result.add(item);
        }
        return result;
    }

    private List<ProductPackageItem> toPackages(List<ProductPackage> packages, String selectedAuditType) {
        List<ProductPackageItem> result = new ArrayList<>();
        for (ProductPackage productPackage : packages) {
            ProductPackageItem item = new ProductPackageItem();
            item.setPackageId(productPackage.getPackageId());
            item.setPackageName(productPackage.getPackageName());
            item.setAnnualPrice(resolvePrice(productPackage, selectedAuditType));
            item.setType1Price(firstPositive(productPackage.getType1Price(), productPackage.getAnnualPrice()));
            item.setType2Price(firstPositive(productPackage.getType2Price(), productPackage.getAnnualPrice()));
            item.setIncludedFeatures(parseJsonList(productPackage.getIncludedFeatures()));
            item.setSupportedTypes(parseJsonList(productPackage.getSupportedTypes()));
            item.setDefaultType(productPackage.getDefaultType());
            result.add(item);
        }
        return result;
    }

    private List<ProductPricingCardItem> toPricingCards(List<ProductPackage> packages, String selectedAuditType) {
        List<ProductPricingCardItem> result = new ArrayList<>();
        for (ProductPackage productPackage : packages) {
            String resolvedAuditType = resolveAuditType(selectedAuditType, productPackage.getDefaultType());
            Integer type1Price = firstPositive(productPackage.getType1Price(), productPackage.getAnnualPrice());
            Integer type2Price = firstPositive(productPackage.getType2Price(), productPackage.getAnnualPrice());
            ProductPricingCardItem cardItem = new ProductPricingCardItem();
            cardItem.setId(productPackage.getPackageId());
            cardItem.setName(productPackage.getPackageName());
            cardItem.setFeatures(toFeatureFlags(parseJsonList(productPackage.getIncludedFeatures())));
            cardItem.setTypeSwitch(SocConstants.AuditType.INTERNAL_TYPE2.equals(resolvedAuditType));
            cardItem.setPrice(SocConstants.AuditType.INTERNAL_TYPE2.equals(resolvedAuditType) ? type2Price : type1Price);
            cardItem.setType1Price(type1Price);
            cardItem.setType2Price(type2Price);
            result.add(cardItem);
        }
        return result;
    }

    private ProductFeatureFlags toFeatureFlags(List<String> features) {
        Set<String> normalizedFeatures = new HashSet<>();
        for (String feature : features) {
            if (feature == null) {
                continue;
            }
            normalizedFeatures.add(normalizeFeatureName(feature));
        }
        ProductFeatureFlags flags = new ProductFeatureFlags();
        flags.setSecurity(normalizedFeatures.contains("security"));
        flags.setAvailability(normalizedFeatures.contains("availability"));
        flags.setPrivacy(normalizedFeatures.contains("privacy"));
        flags.setProcessing(normalizedFeatures.contains("processingintegrity") || normalizedFeatures.contains("processing"));
        flags.setConfidentiality(normalizedFeatures.contains("confidentiality"));
        return flags;
    }

    private String normalizeFeatureName(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", "");
    }

    private Integer resolvePrice(ProductPackage productPackage, String auditType) {
        String normalizedType = resolveAuditType(auditType, productPackage.getDefaultType());
        Integer type1Price = firstPositive(productPackage.getType1Price(), productPackage.getAnnualPrice());
        Integer type2Price = firstPositive(productPackage.getType2Price(), productPackage.getAnnualPrice());
        if (SocConstants.AuditType.INTERNAL_TYPE2.equals(normalizedType)) {
            return type2Price;
        }
        return type1Price;
    }

    private String resolveAuditType(String selectedAuditType, String defaultType) {
        String normalizedSelected = normalizeAuditType(selectedAuditType);
        if (SocConstants.AuditType.INTERNAL_TYPE1.equals(normalizedSelected) || SocConstants.AuditType.INTERNAL_TYPE2.equals(normalizedSelected)) {
            return normalizedSelected;
        }
        String normalizedDefault = normalizeAuditType(defaultType);
        if (SocConstants.AuditType.INTERNAL_TYPE1.equals(normalizedDefault) || SocConstants.AuditType.INTERNAL_TYPE2.equals(normalizedDefault)) {
            return normalizedDefault;
        }
        return SocConstants.AuditType.INTERNAL_TYPE1;
    }

    private String normalizeAuditType(String auditType) {
        if (auditType == null || auditType.isBlank()) {
            return null;
        }
        String normalized = auditType.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", "");
        if (SocConstants.AuditType.INTERNAL_TYPE2.equals(normalized)) {
            return SocConstants.AuditType.INTERNAL_TYPE2;
        }
        if (SocConstants.AuditType.INTERNAL_TYPE1.equals(normalized)) {
            return SocConstants.AuditType.INTERNAL_TYPE1;
        }
        return null;
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

    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {});
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    private Object parseJsonFeatureList(String json) {
        if (json == null || json.isBlank()) {
            return Collections.emptyMap();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Object>() {});
        } catch (Exception exception) {
            return Collections.emptyMap();
        }
    }

    private void ensureCurrentUser() {
        if (userAccountMapper.selectById(currentUserAccessor.requireUserId()) == null) {
            throw new BizException(BizErrorCode.AUTH_CURRENT_USER_NOT_FOUND);
        }
    }
}
