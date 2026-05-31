package com.compliancemind.soc.entity.commerce;

import lombok.Data;

import java.time.LocalDateTime;

/** 在售商品（{@code sys_product}）。 */
@Data
public class Product {

    /** 产品 ID。 */
    private Integer productId;
    /** 产品名称（如 SOC 2、ISO27001）。 */
    private String productName;
    /** 产品编码（唯一）。 */
    private String productCode;
    /** 产品介绍标题。 */
    private String introductionTitle;
    /** 产品介绍正文。 */
    private String introductionText;
    /** 产品 Logo URL。 */
    private String logoUrl;
    /** 信任原则列表（JSON 字符串）。 */
    private String trustPrinciples;
    /** 全部功能列表（JSON 字符串）。 */
    private String allFeatures;
    /** 上架状态（1=上架，0=下架）。 */
    private Integer status;
    /** 排序序号。 */
    private Integer sortNo;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
