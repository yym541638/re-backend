package com.compliancemind.soc.dto.commerce;

import lombok.Data;

@Data
public class ProductPricingCardItem {

    private Integer id;
    private String name;
    private ProductFeatureFlags features;
    private Boolean typeSwitch;
    private Integer price;
    private Integer type1Price;
    private Integer type2Price;
}
