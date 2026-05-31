package com.compliancemind.soc.dto.commerce;

import lombok.Data;

@Data
public class ProductFeatureFlags {

    private boolean security;
    private boolean availability;
    private boolean privacy;
    private boolean processing;
    private boolean confidentiality;
}
