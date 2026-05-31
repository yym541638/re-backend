package com.compliancemind.soc.dto.project;

import lombok.Data;

@Data
public class ProjectQueryRequest {

    private String keyword;
    private String status;
}
