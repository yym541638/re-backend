package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class ProductPriceUpdateRequest {

    @Min(value = 1, message = "type1Price must be greater than 0")
    @JsonAlias({"type1_price"})
    private Integer type1Price;

    @Min(value = 1, message = "type2Price must be greater than 0")
    @JsonAlias({"type2_price"})
    private Integer type2Price;

    @JsonAlias({"default_type"})
    private String defaultType;
}
