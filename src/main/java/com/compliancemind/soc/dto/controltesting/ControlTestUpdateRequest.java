package com.compliancemind.soc.dto.controltesting;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ControlTestUpdateRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;
    private String riskLevel;
    private String riskDescription;
    private String cosoPrinciple;
    private String controlProcedure;
    private String resultStatus;
    private String changeSummary;
}

