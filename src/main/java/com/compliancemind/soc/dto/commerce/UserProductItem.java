package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UserProductItem {

    @JsonProperty("product_id")
    private Integer productId;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("package_id")
    private Integer packageId;
    @JsonProperty("included_features")
    private String includedFeatures;
    @JsonProperty("audit_type")
    private String auditType;
    private String status;
    @JsonProperty("source_order_no")
    private String sourceOrderNo;
    @JsonProperty("start_time")
    private String startTime;
    @JsonProperty("end_time")
    private String endTime;
}
