package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestMasterTemplateFileItem {

    @JsonProperty("template_file_id")
    private Long templateFileId;

    @JsonProperty("file_no")
    private Integer fileNo;

    private String files;

    @JsonProperty("relevant_criteria")
    private String relevantCriteria;
}
