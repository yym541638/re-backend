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
@Service // 注册为 Spring Bean
public class ProductService { // 商品业务服务类

    // 订阅起止时间的统一输出格式（秒级）
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(SocConstants.Format.DATETIME_SECONDS);

    private final ProductMapper productMapper; // 商品与套餐 Mapper
    private final UserProductMapper userProductMapper; // 用户订阅 Mapper
    private final UserAccountMapper userAccountMapper; // 用户账号 Mapper
    private final AuthorizationService authorizationService; // 权限服务
    private final CurrentUserAccessor currentUserAccessor; // 当前用户访问器
    private final ObjectMapper objectMapper; // JSON 工具

    // 构造器注入依赖
    public ProductService(ProductMapper productMapper,
                          UserProductMapper userProductMapper,
                          UserAccountMapper userAccountMapper,
                          AuthorizationService authorizationService,
                          CurrentUserAccessor currentUserAccessor,
                          ObjectMapper objectMapper) {
        this.productMapper = productMapper; // 赋值商品 Mapper
        this.userProductMapper = userProductMapper; // 赋值用户订阅 Mapper
        this.userAccountMapper = userAccountMapper; // 赋值用户账号 Mapper
        this.authorizationService = authorizationService; // 赋值权限服务
        this.currentUserAccessor = currentUserAccessor; // 赋值当前用户访问器
        this.objectMapper = objectMapper; // 赋值 JSON 工具
    }

    // 商品列表：返回所有在售商品概要
    public List<ProductListItem> list() {
        List<ProductListItem> result = new ArrayList<>(); // 初始化结果列表
        for (Product product : productMapper.listActiveProducts()) { // 遍历所有激活商品
            ProductListItem item = new ProductListItem(); // 创建列表项 DTO
            item.setProductId(product.getProductId()); // 设置商品 ID
            item.setProductName(product.getProductName()); // 设置商品名称
            item.setProductCode(product.getProductCode()); // 设置商品编码
            item.setIntroductionText(product.getIntroductionText()); // 设置介绍文案
            item.setLogoUrl(product.getLogoUrl()); // 设置 Logo 地址
            item.setTrustPrinciples(parseJsonList(product.getTrustPrinciples())); // 解析信任原则 JSON 为列表
            result.add(item); // 加入结果集
        }
        return result; // 返回商品列表
    }

    /**
     * 新用户购买页：返回 SOC 2 下全部在售套餐及动态价格，无需传 productId。
     */
    // 按 SOC2 产品码查询套餐详情列表
    public List<ProductDetail2Response> listSoc2Packages(String selectedAuditType) {
        Product product = productMapper.selectByProductCode(SocConstants.Product.CODE_SOC2); // 按固定编码查 SOC2 商品
        if (product == null) { // 商品不存在
            throw new BizException(BizErrorCode.COMMERCE_PRODUCT_NOT_FOUND); // 抛出商品未找到异常
        }
        return buildPackageDetailResponses(product, selectedAuditType); // 组装套餐详情并返回
    }

    // 用户已购产品详情展示
    public List<ProductDetail2Response> purchasedDetail(Integer productId) {
        ensureCurrentUser();
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BizException(BizErrorCode.COMMERCE_PRODUCT_NOT_FOUND);
        }
        UserProduct userProduct = userProductMapper.selectByUserIdAndProductId(
            currentUserAccessor.requireUserId(), productId);
        if (userProduct == null) {
            throw new BizException(BizErrorCode.COMMERCE_USER_PRODUCT_NOT_FOUND);
        }
        return List.of(buildPurchasedDetailResponse(product, userProduct));
    }

    // 将商品下所有套餐转为 ProductDetail2Response 列表
    private List<ProductDetail2Response> buildPackageDetailResponses(Product product, String selectedAuditType) {
        List<ProductDetail2Response> result = new ArrayList<>(); // 初始化结果列表
        List<ProductPackage> packageEntities = productMapper.listPackagesByProductId(product.getProductId()); // 查该商品全部套餐
        for (ProductPackage productPackage : packageEntities) { // 遍历每个套餐
            ProductDetail2Response response = new ProductDetail2Response(); // 创建详情 DTO
            response.setProductId(productPackage.getPackageId()); // 前端展示用 ID 取套餐 ID
            response.setProductName(productPackage.getPackageName()); // 展示名取套餐名
            response.setProductCode(product.getProductCode()); // 商品编码来自父商品
            response.setFeatures(parseJsonFeatureList(product.getAllFeatures())); // 解析商品全量特性 JSON
            response.setTypeSwitch(SocConstants.AuditType.INTERNAL_TYPE2.equals( // 是否选中 Type II
                resolveAuditType(selectedAuditType, productPackage.getDefaultType()))); // 解析当前审计类型
            response.setPrice(resolvePrice(productPackage, selectedAuditType).toString()); // 按审计类型计算价格并转字符串
            result.add(response); // 加入结果集
        }
        return result; // 返回套餐详情列表
    }

    private ProductDetail2Response buildPurchasedDetailResponse(Product product, UserProduct userProduct) {
        ProductDetail2Response response = new ProductDetail2Response();
        response.setProductId(product.getProductId());
        response.setProductName(userProduct.getProductName() != null && !userProduct.getProductName().isBlank()
            ? userProduct.getProductName()
            : product.getProductName());
        response.setProductCode(product.getProductCode());
        response.setFeatures(toIncludedFeatureList(userProduct.getIncludedFeatures()));

        List<ProductPackage> packages = productMapper.listPackagesByProductId(product.getProductId());
        ProductPackage pricingPackage = packages.isEmpty() ? null : packages.get(0);
        String purchasedAuditType = userProduct.getAuditType() != null && !userProduct.getAuditType().isBlank()
            ? userProduct.getAuditType()
            : (pricingPackage == null ? null : pricingPackage.getDefaultType());
        if (pricingPackage != null) {
            String resolvedAuditType = resolveAuditType(null, purchasedAuditType);
            response.setTypeSwitch(SocConstants.AuditType.INTERNAL_TYPE2.equals(resolvedAuditType));
            response.setPrice(resolvePrice(pricingPackage, purchasedAuditType).toString());
        } else {
            response.setTypeSwitch(false);
            response.setPrice("0");
        }
        return response;
    }

    // 管理员更新套餐 Type I / Type II 价格及默认审计类型
    public ProductPackageItem updatePackagePrice(Integer packageId, Integer type1Price, Integer type2Price, String defaultType) {
        authorizationService.requireCompanyManagement(); // 校验当前用户具备公司管理权限
        ProductPackage productPackage = productMapper.selectPackageById(packageId); // 按 ID 查套餐
        if (productPackage == null) { // 套餐不存在
            throw new BizException(BizErrorCode.COMMERCE_PACKAGE_NOT_FOUND); // 抛出套餐未找到异常
        }
        Integer resolvedType1Price = firstPositive(type1Price, productPackage.getType1Price()); // 解析 Type I 价格（新值优先，否则沿用旧值）
        Integer resolvedType2Price = firstPositive(type2Price, productPackage.getType2Price()); // 解析 Type II 价格
        if (resolvedType1Price <= 0 || resolvedType2Price <= 0) { // 任一价格无效
            throw new BizException(BizErrorCode.COMMERCE_PRICE_INVALID); // 抛出价格无效异常
        }
        String resolvedDefaultType = resolveAuditType(defaultType, productPackage.getDefaultType()); // 解析默认审计类型
        String persistedDefaultType = SocConstants.AuditType.INTERNAL_TYPE2.equals(resolvedDefaultType) // 若默认 Type II
            ? SocConstants.AuditType.DISPLAY_TYPE2 // 存库用展示值 Type2
            : SocConstants.AuditType.DISPLAY_TYPE1; // 否则存 Type1
        productMapper.updatePackagePrice(packageId, resolvedType1Price, resolvedType2Price, persistedDefaultType); // 持久化价格与默认类型
        ProductPackage latest = productMapper.selectPackageById(packageId); // 重新读取最新套餐
        ProductPackageItem item = new ProductPackageItem(); // 创建返回 DTO
        item.setPackageId(latest.getPackageId()); // 设置套餐 ID
        item.setPackageName(latest.getPackageName()); // 设置套餐名称
        item.setAnnualPrice(resolvePrice(latest, resolvedDefaultType)); // 按默认类型计算展示年费
        item.setType1Price(firstPositive(latest.getType1Price(), latest.getAnnualPrice())); // 设置 Type I 价格
        item.setType2Price(firstPositive(latest.getType2Price(), latest.getAnnualPrice())); // 设置 Type II 价格
        item.setIncludedFeatures(parseJsonList(latest.getIncludedFeatures())); // 解析包含特性列表
        item.setSupportedTypes(parseJsonList(latest.getSupportedTypes())); // 解析支持的审计类型列表
        item.setDefaultType(latest.getDefaultType()); // 设置默认审计类型
        return item; // 返回更新后的套餐信息
    }

    // 当前登录用户已购产品/模块列表
    public List<UserProductItem> myProducts() {
        ensureCurrentUser(); // 校验当前用户存在
        List<UserProductItem> result = new ArrayList<>(); // 初始化结果列表
        for (UserProduct userProduct : userProductMapper.listByUserId(currentUserAccessor.requireUserId())) { // 查当前用户全部订阅
            UserProductItem item = new UserProductItem(); // 创建列表项 DTO
            item.setProductId(userProduct.getProductId()); // 设置商品 ID
            item.setProductName(userProduct.getProductName()); // 设置商品名称
            item.setPackageId(userProduct.getPackageId()); // 设置套餐 ID
            item.setIncludedFeatures(formatIncludedFeaturesText(userProduct.getIncludedFeatures())); // 设置已购套餐能力（逗号分隔）
            item.setAuditType(userProduct.getAuditType()); // 设置审计类型
            item.setStatus(userProduct.getStatus()); // 设置订阅状态
            item.setSourceOrderNo(userProduct.getSourceOrderNo()); // 设置来源订单号
            item.setStartTime(userProduct.getStartTime() == null ? null : userProduct.getStartTime().format(DATE_TIME_FORMATTER)); // 格式化开始时间
            item.setEndTime(userProduct.getEndTime() == null ? null : userProduct.getEndTime().format(DATE_TIME_FORMATTER)); // 格式化结束时间
            result.add(item); // 加入结果集
        }
        return result; // 返回已购产品列表
    }

    // 将套餐实体列表转为 ProductPackageItem 列表（含动态价格）
    private List<ProductPackageItem> toPackages(List<ProductPackage> packages, String selectedAuditType) {
        List<ProductPackageItem> result = new ArrayList<>(); // 初始化结果列表
        for (ProductPackage productPackage : packages) { // 遍历套餐
            ProductPackageItem item = new ProductPackageItem(); // 创建 DTO
            item.setPackageId(productPackage.getPackageId()); // 设置套餐 ID
            item.setPackageName(productPackage.getPackageName()); // 设置套餐名称
            item.setAnnualPrice(resolvePrice(productPackage, selectedAuditType)); // 按选中审计类型计算年费
            item.setType1Price(firstPositive(productPackage.getType1Price(), productPackage.getAnnualPrice())); // Type I 价格
            item.setType2Price(firstPositive(productPackage.getType2Price(), productPackage.getAnnualPrice())); // Type II 价格
            item.setIncludedFeatures(parseJsonList(productPackage.getIncludedFeatures())); // 包含特性
            item.setSupportedTypes(parseJsonList(productPackage.getSupportedTypes())); // 支持审计类型
            item.setDefaultType(productPackage.getDefaultType()); // 默认审计类型
            result.add(item); // 加入结果集
        }
        return result; // 返回套餐 DTO 列表
    }

    // 将套餐实体列表转为定价卡片 DTO 列表
    private List<ProductPricingCardItem> toPricingCards(List<ProductPackage> packages, String selectedAuditType) {
        List<ProductPricingCardItem> result = new ArrayList<>(); // 初始化结果列表
        for (ProductPackage productPackage : packages) { // 遍历套餐
            String resolvedAuditType = resolveAuditType(selectedAuditType, productPackage.getDefaultType()); // 解析审计类型
            Integer type1Price = firstPositive(productPackage.getType1Price(), productPackage.getAnnualPrice()); // Type I 价格
            Integer type2Price = firstPositive(productPackage.getType2Price(), productPackage.getAnnualPrice()); // Type II 价格
            ProductPricingCardItem cardItem = new ProductPricingCardItem(); // 创建定价卡片 DTO
            cardItem.setId(productPackage.getPackageId()); // 卡片 ID 取套餐 ID
            cardItem.setName(productPackage.getPackageName()); // 卡片名称
            cardItem.setFeatures(toFeatureFlags(parseJsonList(productPackage.getIncludedFeatures()))); // 特性转为布尔标志
            cardItem.setTypeSwitch(SocConstants.AuditType.INTERNAL_TYPE2.equals(resolvedAuditType)); // 是否 Type II
            cardItem.setPrice(SocConstants.AuditType.INTERNAL_TYPE2.equals(resolvedAuditType) ? type2Price : type1Price); // 当前展示价格
            cardItem.setType1Price(type1Price); // 设置 Type I 价格
            cardItem.setType2Price(type2Price); // 设置 Type II 价格
            result.add(cardItem); // 加入结果集
        }
        return result; // 返回定价卡片列表
    }

    // 将特性名称列表转为 SOC2 五大信任原则布尔标志
    private ProductFeatureFlags toFeatureFlags(List<String> features) {
        Set<String> normalizedFeatures = new HashSet<>(); // 归一化后的特性名集合
        for (String feature : features) { // 遍历特性名
            if (feature == null) { // 跳过空值
                continue; // 继续下一项
            }
            normalizedFeatures.add(normalizeFeatureName(feature)); // 归一化后加入集合
        }
        ProductFeatureFlags flags = new ProductFeatureFlags(); // 创建特性标志 DTO
        flags.setSecurity(normalizedFeatures.contains("security")); // 是否包含 Security
        flags.setAvailability(normalizedFeatures.contains("availability")); // 是否包含 Availability
        flags.setPrivacy(normalizedFeatures.contains("privacy")); // 是否包含 Privacy
        flags.setProcessing(normalizedFeatures.contains("processingintegrity") || normalizedFeatures.contains("processing")); // Processing Integrity
        flags.setConfidentiality(normalizedFeatures.contains("confidentiality")); // 是否包含 Confidentiality
        return flags; // 返回特性标志
    }

    // 特性名归一化：小写并移除非字母字符
    private String normalizeFeatureName(String value) {
        return value.toLowerCase(Locale.ROOT).replaceAll("[^a-z]", ""); // 转小写并 strip 非 a-z
    }

    // 按审计类型解析套餐应展示的价格
    private Integer resolvePrice(ProductPackage productPackage, String auditType) {
        String normalizedType = resolveAuditType(auditType, productPackage.getDefaultType()); // 解析审计类型
        Integer type1Price = firstPositive(productPackage.getType1Price(), productPackage.getAnnualPrice()); // Type I 价格
        Integer type2Price = firstPositive(productPackage.getType2Price(), productPackage.getAnnualPrice()); // Type II 价格
        if (SocConstants.AuditType.INTERNAL_TYPE2.equals(normalizedType)) { // 若为 Type II
            return type2Price; // 返回 Type II 价格
        }
        return type1Price; // 默认返回 Type I 价格
    }

    // 解析最终审计类型：优先用户选择，其次套餐默认，最后 fallback 为 Type I
    private String resolveAuditType(String selectedAuditType, String defaultType) {
        String normalizedSelected = normalizeAuditType(selectedAuditType); // 归一化用户选择
        if (SocConstants.AuditType.INTERNAL_TYPE1.equals(normalizedSelected) || SocConstants.AuditType.INTERNAL_TYPE2.equals(normalizedSelected)) { // 选择有效
            return normalizedSelected; // 使用用户选择
        }
        String normalizedDefault = normalizeAuditType(defaultType); // 归一化套餐默认类型
        if (SocConstants.AuditType.INTERNAL_TYPE1.equals(normalizedDefault) || SocConstants.AuditType.INTERNAL_TYPE2.equals(normalizedDefault)) { // 默认有效
            return normalizedDefault; // 使用套餐默认
        }
        return SocConstants.AuditType.INTERNAL_TYPE1; // 兜底 Type I
    }

    // 将各种格式的审计类型字符串归一化为内部 TYPE1 / TYPE2
    private String normalizeAuditType(String auditType) {
        if (auditType == null || auditType.isBlank()) { // 空值
            return null; // 返回 null 表示无效
        }
        String normalized = auditType.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]", ""); // 大写并移除非字母数字
        if (SocConstants.AuditType.INTERNAL_TYPE2.equals(normalized)) { // 匹配 Type II
            return SocConstants.AuditType.INTERNAL_TYPE2; // 返回内部 Type II 常量
        }
        if (SocConstants.AuditType.INTERNAL_TYPE1.equals(normalized)) { // 匹配 Type I
            return SocConstants.AuditType.INTERNAL_TYPE1; // 返回内部 Type I 常量
        }
        return null; // 无法识别则返回 null
    }

    // 取第一个正整数：优先 first，否则 fallback，都无效则 0
    private Integer firstPositive(Integer first, Integer fallback) {
        if (first != null && first > 0) { // first 有效
            return first; // 返回 first
        }
        if (fallback != null && fallback > 0) { // fallback 有效
            return fallback; // 返回 fallback
        }
        return 0; // 均无效返回 0
    }

    // 将 JSON 字符串解析为字符串列表，失败或空则返回空列表
    private List<String> parseJsonList(String json) {
        if (json == null || json.isBlank()) { // 空 JSON
            return Collections.emptyList(); // 返回空列表
        }
        try {
            return objectMapper.readValue(json, new TypeReference<List<String>>() {}); // 反序列化为 List<String>
        } catch (Exception exception) { // 解析失败
            return Collections.emptyList(); // 降级为空列表
        }
    }

    // 将库中能力字段格式化为逗号分隔字符串（兼容历史 JSON 数组）
    private String formatIncludedFeaturesText(String stored) {
        if (stored == null || stored.isBlank()) {
            return "";
        }
        String trimmed = stored.trim();
        if (trimmed.startsWith("[")) {
            return String.join(",", parseJsonList(trimmed));
        }
        return trimmed;
    }

    private List<String> toIncludedFeatureList(String stored) {
        String text = formatIncludedFeaturesText(stored);
        if (text.isBlank()) {
            return Collections.emptyList();
        }
        List<String> features = new ArrayList<>();
        for (String feature : text.split(",")) {
            if (feature != null && !feature.isBlank()) {
                features.add(feature.trim());
            }
        }
        return features;
    }

    // 将 JSON 字符串解析为通用 Object（Map/List 等），失败或空则返回空 Map
    private Object parseJsonFeatureList(String json) {
        if (json == null || json.isBlank()) { // 空 JSON
            return Collections.emptyMap(); // 返回空 Map
        }
        try {
            return objectMapper.readValue(json, new TypeReference<Object>() {}); // 反序列化为 Object
        } catch (Exception exception) { // 解析失败
            return Collections.emptyMap(); // 降级为空 Map
        }
    }

    // 校验 JWT 上下文中的用户 ID 在数据库中仍存在
    private void ensureCurrentUser() {
        if (userAccountMapper.selectById(currentUserAccessor.requireUserId()) == null) { // 用户不存在
            throw new BizException(BizErrorCode.AUTH_CURRENT_USER_NOT_FOUND); // 抛出当前用户未找到异常
        }
    }
}
