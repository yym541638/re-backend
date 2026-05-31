package com.compliancemind.soc.entity.controltesting;

import lombok.Data;

import java.time.LocalDateTime;

/** 控制测试版本快照（{@code soc_control_test_version}）。 */
@Data
public class ControlTestVersion {

    /** 版本 ID。 */
    private Long versionId;
    /** 所属控制测试 ID。 */
    private Long testId;
    /** 版本号（如 V1、V2）。 */
    private String versionNo;
    /** 控制测试完整快照（JSON）。 */
    private String snapshotJson;
    /** 变更摘要。 */
    private String changeSummary;
    /** 创建人用户 ID。 */
    private Integer createdBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
