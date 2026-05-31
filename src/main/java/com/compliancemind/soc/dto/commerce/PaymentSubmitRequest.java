package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentSubmitRequest {

    @JsonAlias({"order_no"})
    private String orderNo;

    @NotNull(message = "产品不能为空")
    @JsonAlias({"product_id"})
    private Integer productId;

    @NotNull(message = "套餐不能为空")
    @JsonAlias({"package_id"})
    private Integer packageId;

    @JsonAlias({"audit_type"})
    private String auditType;

    private Integer amount;

    @JsonAlias({"payment_method"})
    private String paymentMethod;

    @JsonAlias({"return_url"})
    private String returnUrl;

    @JsonAlias({"notify_url"})
    private String notifyUrl;
}
