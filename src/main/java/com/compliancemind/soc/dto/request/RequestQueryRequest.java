package com.compliancemind.soc.dto.request;

import lombok.Data;

@Data
public class RequestQueryRequest {

    private Long projectId;
    private String documentStatus;
    private String ccCriteria;
}
