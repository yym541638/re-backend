package com.compliancemind.soc.entity.risk;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/** 项目级风险表行（{@code soc_risk}）。 */
@Data
public class RiskRecord {

    @JsonProperty("risk_id")
    private Long riskId;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("cc_criteria")
    private String ccCriteria;

    @JsonProperty("cycle_name")
    private String cycleName;

    @JsonProperty("modules_id")
    private String modulesId;

    @JsonProperty("modules_name")
    private String modulesName;

    @JsonProperty("cc_criteria_name")
    private String ccCriteriaName;

    @JsonProperty("sub_risk_id")
    private String subRiskId;

    @JsonProperty("sub_risk_name")
    private String subRiskName;

    @JsonProperty("points_of_focus_id")
    private String pointsOfFocusId;

    @JsonProperty("points_of_focus_name")
    private String pointsOfFocusName;

    @JsonProperty("risk_level")
    private String riskLevel;

    @JsonProperty("risk_source")
    private String riskSource;

    @JsonProperty("additional_risk_profile_description")
    private String additionalRiskProfileDescription;

    private Integer deleted;
    private Integer createdBy;
    private Integer updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
