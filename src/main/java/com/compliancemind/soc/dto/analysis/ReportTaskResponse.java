package com.compliancemind.soc.dto.analysis;

import lombok.Data;

@Data
public class ReportTaskResponse {

    private String taskId;
    private String status;
    private Integer progress;
    private String downloadUrl;
}

