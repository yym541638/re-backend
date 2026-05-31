package com.compliancemind.soc.dto.rcm;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RcmAiGenerateRequest {

    @NotNull(message = "项目不能为空")
    @JsonAlias({"project_id"})
    private Long projectId;

    @NotBlank(message = "公司描述不能为空")
    @JsonAlias({"company_description"})
    private String companyDescription;

    @NotBlank(message = "系统描述不能为空")
    @JsonAlias({"system_description"})
    private String systemDescription;

    @JsonAlias({"compliance_framework"})
    private String complianceFramework;
    @JsonAlias({"source_request_id"})
    private Long sourceRequestId;
}
