package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProductDetail2Response {
    @JsonProperty("product_id")
    private Integer productId;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty("features")
    private Object features;
    @JsonProperty("type_switch")
    private boolean typeSwitch;
    @JsonProperty("price")
    private String price;
}
