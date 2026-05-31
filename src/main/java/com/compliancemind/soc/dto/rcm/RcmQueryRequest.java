package com.compliancemind.soc.dto.rcm;

import lombok.Data;

@Data
public class RcmQueryRequest {

    private Long projectId;
    private String status;
    private String category;
    private String riskRating;
    private String stage;
    private Long sourceRequestId;
}
