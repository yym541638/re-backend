package com.compliancemind.soc.dto.analysis;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TrendPoint {

    private LocalDate snapshotDate;
    private BigDecimal passRate;
    private long total;
    private long passed;
    private long failed;
    private long pending;
    private long gapCount;
}

