package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestDocumentOwnerItem {

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("display_name")
    private String displayName;

    private String email;
}
