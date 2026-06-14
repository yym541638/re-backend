package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/** Request Master 详情 / 表单回显。 */
@Data
public class RequestMasterDetailResponse {

    @JsonProperty("request_master_id")
    private Long requestMasterId;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("request_id")
    @JsonAlias({"request_master_code"})
    private String requestId;

    @JsonProperty("request_master_name")
    private String requestMasterName;

    /** 仅显示，不可修改。 */
    @JsonProperty("create_date")
    @JsonAlias({"request_master_create_date", "created_at"})
    private LocalDateTime createDate;

    @JsonProperty("request_master_status")
    @JsonAlias({"status", "RequestMasterStatus"})
    private String requestMasterStatus;
}
