package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/** Request Master 列表行。 */
@Data
public class RequestMasterListItem {

    @JsonProperty("request_master_id")
    private Long requestMasterId;

    /** UI 列 request ID，对应 ReqM000000 格式编码。 */
    @JsonProperty("request_id")
    @JsonAlias({"request_master_code"})
    private String requestId;

    @JsonProperty("request_master_name")
    private String requestMasterName;

    @JsonProperty("request_master_create_date")
    @JsonAlias({"create_date", "created_at"})
    private LocalDateTime requestMasterCreateDate;

    @JsonProperty("request_master_status")
    @JsonAlias({"status", "RequestMasterStatus"})
    private String requestMasterStatus;
}
