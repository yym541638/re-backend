package com.compliancemind.soc.entity.commerce;

import lombok.Data;

import java.time.LocalDateTime;

/** 商品套餐与定价（{@code sys_product_package}）。 */
@Data
public class ProductPackage {

    /** 套餐 ID。 */
    private Integer packageId;
    /** 所属产品 ID。 */
    private Integer productId;
    /** 套餐名称（如 Basic 3、Product Suite）。 */
    private String packageName;
    /** 年费基准价（分/最小货币单位）。 */
    private Integer annualPrice;
    /** Type1 审计类型价格。 */
    private Integer type1Price;
    /** Type2 审计类型价格。 */
    private Integer type2Price;
    /** 套餐包含功能列表（JSON 字符串）。 */
    private String includedFeatures;
    /** 支持的审计类型列表（JSON 字符串）。 */
    private String supportedTypes;
    /** 默认审计类型（Type1 / Type2）。 */
    private String defaultType;
    /** 上架状态（1=上架，0=下架）。 */
    private Integer status;
    /** 排序序号。 */
    private Integer sortNo;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
