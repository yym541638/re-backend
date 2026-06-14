package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class RequestMasterVersionDetailResponse {

    @JsonProperty("version_id")
    private Long versionId;

    @JsonProperty("version_label")
    private String versionLabel;

    @JsonProperty("individuals")
    private List<RequestIndividualListItem> individuals;
}
