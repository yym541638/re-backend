package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProductListItem {

    @JsonProperty("product_id")
    private Integer productId;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty("intro_text")
    private String introductionText;
    @JsonProperty("logo_url")
    private String logoUrl;
    @JsonProperty("trust_principles")
    private List<String> trustPrinciples;
}
