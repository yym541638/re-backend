package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestMasterCreateRequest {

    @NotNull(message = "项目不能为空")
    @JsonAlias({"project_id"})
    private Long projectId;

    @NotBlank(message = "Request Master 名称不能为空")
    @JsonAlias({"request_master_name", "name"})
    private String requestMasterName;

    @JsonAlias({"request_master_status", "status", "RequestMasterStatus"})
    private String requestMasterStatus;
}
