package com.compliancemind.soc.entity.commerce;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户订阅实例（{@code sys_user_product}）。 */
@Data
public class UserProduct {

    /** 订阅记录 ID。 */
    private Long userProductId;
    /** 用户 ID。 */
    private Integer userId;
    /** 产品 ID。 */
    private Integer productId;
    /** 产品名称快照。 */
    private String productName;
    /** 套餐 ID。 */
    private Integer packageId;
    /** 套餐名称快照。 */
    private String packageName;
    /** 审计类型（Type1 / Type2）。 */
    private String auditType;
    /** 来源订单号。 */
    private String sourceOrderNo;
    /** 订阅状态（如 ACTIVE）。 */
    private String status;
    /** 订阅生效时间。 */
    private LocalDateTime startTime;
    /** 订阅到期时间。 */
    private LocalDateTime endTime;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
