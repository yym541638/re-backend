package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ProductDetailResponse {

    @JsonProperty("product_id")
    private Integer productId;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("product_code")
    private String productCode;
    @JsonProperty("introduction_title")
    private String introductionTitle;
    @JsonProperty("introduction_text")
    private String introductionText;
    @JsonProperty("logo_url")
    private String logoUrl;
    @JsonProperty("trust_principles")
    private List<String> trustPrinciples;
    @JsonProperty("all_features")
    private List<String> allFeatures;
    private List<ProductPackageItem> packages;
    private List<ProductPricingCardItem> products;
}
