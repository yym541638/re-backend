package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentSubmitResponse {

    @JsonProperty("order_no")
    private String orderNo;
    @JsonProperty("payment_url")
    private String paymentUrl;
    @JsonProperty("qr_code")
    private String qrCode;
    @JsonProperty("expire_time")
    private String expireTime;
    private Integer amount;
    @JsonProperty("annual_price")
    private Integer annualPrice;
    @JsonProperty("audit_type")
    private String auditType;
}
