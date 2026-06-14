package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestEvidenceItem {

    @JsonProperty("attachment_id")
    private Long attachmentId;

    private String file;

    private LocalDateTime time;
}
