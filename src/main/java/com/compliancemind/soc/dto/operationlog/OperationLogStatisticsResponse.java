package com.compliancemind.soc.dto.operationlog;

import lombok.Data;

import java.util.Map;

@Data
public class OperationLogStatisticsResponse {

    private long total;
    private Map<String, Long> byModule;
    private Map<String, Long> byActionType;
}

