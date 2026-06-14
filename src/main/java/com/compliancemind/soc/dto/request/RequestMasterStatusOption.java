package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** RequestMasterStatus 下拉选项。 */
@Data
public class RequestMasterStatusOption {

    @JsonProperty("status")
    private String status;

    @JsonProperty("label")
    private String label;
}
