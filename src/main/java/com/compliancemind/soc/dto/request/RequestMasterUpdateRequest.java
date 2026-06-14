package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestMasterUpdateRequest {

    @NotBlank(message = "Request Master 名称不能为空")
    @JsonAlias({"request_master_name", "name"})
    private String requestMasterName;

    @JsonAlias({"request_master_status", "status", "RequestMasterStatus"})
    private String requestMasterStatus;
}
