package com.compliancemind.soc.entity.rcm;

import lombok.Data;

import java.time.LocalDateTime;

/** RCM 版本快照（{@code soc_rcm_version}）。 */
@Data
public class RcmVersion {

    /** 版本 ID。 */
    private Long versionId;
    /** 所属 RCM 记录 ID。 */
    private Long rcmId;
    /** 版本号（如 V1、V2）。 */
    private String versionNo;
    /** RCM 完整快照（JSON）。 */
    private String snapshotJson;
    /** 变更摘要。 */
    private String changeSummary;
    /** 创建人用户 ID。 */
    private Integer createdBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
