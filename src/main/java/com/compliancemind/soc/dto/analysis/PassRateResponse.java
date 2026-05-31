package com.compliancemind.soc.dto.analysis;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PassRateResponse {

    private Long projectId;
    private long total;
    private long passed;
    private long failed;
    private long pending;
    private long gapCount;
    private BigDecimal passRate;
    private String assessment;
}

