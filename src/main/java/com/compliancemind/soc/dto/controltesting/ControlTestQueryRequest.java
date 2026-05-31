package com.compliancemind.soc.dto.controltesting;

import lombok.Data;

@Data
public class ControlTestQueryRequest {

    private Long projectId;
    private String resultStatus;
    private String riskLevel;
}
