package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProductPackageItem {

    @JsonProperty("package_id")
    private Integer packageId;
    @JsonProperty("package_name")
    private String packageName;
    @JsonProperty("annual_price")
    private Integer annualPrice;
    @JsonProperty("type1_price")
    private Integer type1Price;
    @JsonProperty("type2_price")
    private Integer type2Price;
    @JsonProperty("included_features")
    private List<String> includedFeatures;
    @JsonProperty("supported_types")
    private List<String> supportedTypes;
    @JsonProperty("default_type")
    private String defaultType;
}
