package com.compliancemind.soc.entity.request;

import lombok.Data;

import java.time.LocalDateTime;

/** 标准条款目录明细行（{@code soc_request_criteria_catalog}）。 */
@Data
public class RequestCriteriaCatalog {

    private Long catalogId;
    private String criteriaCode;
    private String moduleName;
    private String requirement;
    private String pointsOfFocus;
    private String documentDescription;
    private Integer sortOrder;
    private Integer deleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
