package com.compliancemind.soc.dto.analysis;

import lombok.Data;

@Data
public class GapAnalysisQueryRequest {

    private Long projectId;
    private String gapLevel;
    private String status;
}

