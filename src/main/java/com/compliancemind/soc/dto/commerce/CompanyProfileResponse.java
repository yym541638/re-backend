package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CompanyProfileResponse {

    @JsonProperty("company_id")
    private Integer companyId;
    @JsonProperty("company_name")
    private String companyName;
    @JsonProperty("company_code")
    private String companyCode;
    private String industry;
    private String website;
    @JsonProperty("contact_name")
    private String contactName;
    @JsonProperty("contact_phone")
    private String contactPhone;
    private String address;
}
