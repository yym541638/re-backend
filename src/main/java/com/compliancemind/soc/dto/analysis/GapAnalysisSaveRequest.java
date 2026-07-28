package com.compliancemind.soc.dto.analysis;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class GapAnalysisSaveRequest {

    @JsonAlias({"source_test_id", "control_id", "controlId"})
    private Long sourceTestId;

    @JsonAlias({"control_title", "issue_title", "issueTitle"})
    private String controlTitle;

    @JsonAlias({"gap_description", "issue_description", "issueDescription"})
    private String gapDescription;

    @JsonAlias({"remediation_suggestion", "remediation_plan", "remediationPlan"})
    private String remediationSuggestion;

    /** YES / NO */
    @JsonAlias({"remediation", "remediation_yes_no"})
    private String remediationYesNo;

    @JsonAlias({"gap_level"})
    private String gapLevel;

    @JsonAlias({"control_description", "controlDescription"})
    private String controlDescription;
}
