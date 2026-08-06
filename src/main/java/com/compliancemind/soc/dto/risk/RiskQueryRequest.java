package com.compliancemind.soc.dto.risk;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

@Data
public class RiskQueryRequest {

    @JsonAlias({"project_id"})
    private Long projectId;

    @JsonAlias({"cc_criteria", "ccCriteria"})
    private String ccCriteria;

    @JsonAlias({"risk_level"})
    private String riskLevel;

    @JsonAlias({"risk_source"})
    private String riskSource;

    @JsonAlias({"keyword", "request_description"})
    private String keyword;

    private Integer pageNum = 1;
    private Integer pageSize = 10;
}
