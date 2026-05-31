package com.compliancemind.soc.dto.operationlog;

import lombok.Data;

@Data
public class OperationLogQueryRequest {

    private String moduleName;
    private String actionType;
    private Long projectId;
}
