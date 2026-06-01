package com.compliancemind.soc.entity.commerce;

import lombok.Data;

import java.time.LocalDateTime;

/** 订单记录（{@code sys_order}）。 */
@Data
public class OrderRecord {

    /** 订单 ID。 */
    private Long orderId;
    /** 订单号（唯一，对外展示）。 */
    private String orderNo;
    /** 下单用户 ID。 */
    private Integer userId;
    /** 购买产品 ID。 */
    private Integer productId;
    /** 购买套餐 ID。 */
    private Integer packageId;
    /** 产品名称快照。 */
    private String productName;
    /** 套餐名称快照。 */
    private String packageName;
    /** 审计类型（Type1 / Type2）。 */
    private String auditType;
    /** 套餐能力快照（JSON 数组字符串）。 */
    private String includedFeatures;
    /** 订单金额（分/最小货币单位）。 */
    private Integer amount;
    /** 支付方式（如 PAYPAL、QRCODE）。 */
    private String paymentMethod;
    /** 订单状态（如 PENDING、PAID、CANCELLED）。 */
    private String status;
    /** 第三方支付交易号。 */
    private String transactionId;
    /** 支付完成跳转 URL。 */
    private String returnUrl;
    /** 支付异步通知 URL。 */
    private String notifyUrl;
    /** 支付完成时间。 */
    private LocalDateTime payTime;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
