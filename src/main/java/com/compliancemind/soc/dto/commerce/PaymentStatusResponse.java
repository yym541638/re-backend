package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PaymentStatusResponse {

    @JsonProperty("order_no")
    private String orderNo;
    private String status;
    private Integer amount;
    @JsonProperty("transaction_id")
    private String transactionId;
    @JsonProperty("pay_time")
    private String payTime;
}
