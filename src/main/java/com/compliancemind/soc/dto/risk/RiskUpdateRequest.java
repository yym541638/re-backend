package com.compliancemind.soc.dto.risk;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class RiskUpdateRequest {

    @JsonAlias({"cc_criteria", "CC Criteria"})
    private String ccCriteria;

    @JsonAlias({"cycle_name", "Cycle_Name"})
    private String cycleName;

    @JsonAlias({"modules_id", "Modules_ID"})
    private String modulesId;

    @JsonAlias({"modules_name", "Modules_Name"})
    private String modulesName;

    @JsonAlias({"cc_criteria_name", "CC_criteria_Name"})
    private String ccCriteriaName;

    @JsonAlias({"sub_risk_id", "Sub-riskID"})
    private String subRiskId;

    @JsonAlias({"sub_risk_name", "Sub-riskname", "Sub-riskName"})
    private String subRiskName;

    @JsonAlias({"points_of_focus_id", "POINTS_OF_FOCUS_ID"})
    private String pointsOfFocusId;

    @JsonAlias({"points_of_focus_name", "POINTS_OF_FOCUS_Name"})
    private String pointsOfFocusName;

    @JsonAlias({"risk_level", "Risk_Level"})
    private String riskLevel;

    @JsonAlias({"risk_source", "Risk Source"})
    private String riskSource;

    @JsonAlias({"additional_risk_profile_description", "Additional Risk Profile Description"})
    private String additionalRiskProfileDescription;
}
