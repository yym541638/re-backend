package com.compliancemind.soc.entity.request;

import lombok.Data;

import java.time.LocalDateTime;

/** 请求历史版本快照（{@code soc_request_version}）。 */
@Data
public class RequestVersion {

    /** 版本 ID。 */
    private Long versionId;
    /** 所属请求 ID。 */
    private Long requestId;
    /** 版本号（如 V1、V2）。 */
    private String versionNo;
    /** 请求完整快照（JSON）。 */
    private String snapshotJson;
    /** 变更摘要。 */
    private String changeSummary;
    /** 创建人用户 ID。 */
    private Integer createdBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
