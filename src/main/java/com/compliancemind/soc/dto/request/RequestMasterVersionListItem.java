package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RequestMasterVersionListItem {

    @JsonProperty("version_id")
    private Long versionId;

    @JsonProperty("version_label")
    private String versionLabel;

    @JsonProperty("is_latest")
    private Boolean latest;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
