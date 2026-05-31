package com.compliancemind.soc.dto.commerce;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CompanyProfileUpdateRequest {

    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    private String companyCode;
    private String industry;
    private String website;
    private String contactName;
    private String contactPhone;
    private String address;
}
