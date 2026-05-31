package com.compliancemind.soc.dto.rcm;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RcmCreateRequest {

    @NotNull(message = "项目不能为空")
    @JsonAlias({"project_id"})
    private Long projectId;

    @NotBlank(message = "控制代码不能为空")
    @JsonAlias({"control_code"})
    private String controlCode;

    @NotBlank(message = "控制名称不能为空")
    @JsonAlias({"control_name"})
    private String controlName;

    private String description;
    private String category;
    @JsonAlias({"module_name", "module"})
    private String moduleName;
    @JsonAlias({"risk_description", "risk"})
    private String riskDescription;
    private String status;
    private String stage;
    @JsonAlias({"is_ai_generated"})
    private Boolean aiGenerated;
    @JsonAlias({"source_request_id"})
    private Long sourceRequestId;
    @JsonAlias({"control_objective"})
    private String controlObjective;
    @JsonAlias({"implementation_method"})
    private String implementationMethod;
    @JsonAlias({"evidence_requirement"})
    private String evidenceRequirement;
    @JsonAlias({"control_performer"})
    private String controlPerformer;
    @JsonAlias({"control_reviewer"})
    private String controlReviewer;
    @JsonAlias({"additional_owner"})
    private String additionalOwner;
    @JsonAlias({"control_risk_rating", "risk_rating"})
    private String controlRiskRating;
}
