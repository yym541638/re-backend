package com.compliancemind.soc.entity.request;

import lombok.Data;

import java.time.LocalDateTime;

/** Request Master 版本快照（{@code soc_request_master_version}）。 */
@Data
public class RequestMasterVersion {

    private Long versionId;
    private Long requestMasterId;
    private String versionLabel;
    private String snapshotJson;
    private Integer isLatest;
    private Integer createdBy;
    private LocalDateTime createdAt;
}
