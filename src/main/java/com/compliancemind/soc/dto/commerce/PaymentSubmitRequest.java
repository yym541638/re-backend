package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class PaymentSubmitRequest {

    @JsonAlias({"order_no"})
    private String orderNo;

    @JsonAlias({"product_id"})
    private Integer productId;

    @JsonAlias({"package_id"})
    private Integer packageId;

    @JsonAlias({"audit_type"})
    private String auditType;

    private Integer amount;

    @JsonAlias({"payment_method"})
    private String paymentMethod;

    /** 用户选中的套餐能力，逗号分隔，如 "Security,Availability,Privacy"。 */
    @JsonAlias({"select_features"})
    private String selectFeatures;
}
