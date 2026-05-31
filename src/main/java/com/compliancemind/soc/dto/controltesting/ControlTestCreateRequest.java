package com.compliancemind.soc.dto.controltesting;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ControlTestCreateRequest {

    @NotNull(message = "项目不能为空")
    private Long projectId;

    @NotBlank(message = "标题不能为空")
    private String title;

    private String description;
    private String riskLevel;
    private String riskDescription;
    private String cosoPrinciple;
    private String controlProcedure;
    private String resultStatus;
}

