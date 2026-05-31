package com.compliancemind.soc.entity.analysis;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** 项目通过率等指标按日快照（{@code soc_score_snapshot}）。 */
@Data
public class ScoreSnapshot {

    /** 快照 ID。 */
    private Long snapshotId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 快照日期。 */
    private LocalDate snapshotDate;
    /** 控制测试总数。 */
    private Long totalCount;
    /** 通过数量。 */
    private Long passedCount;
    /** 失败数量。 */
    private Long failedCount;
    /** 待处理数量。 */
    private Long pendingCount;
    /** 差距条目数量。 */
    private Long gapCount;
    /** 通过率（百分比）。 */
    private BigDecimal passRate;
    /** 综合评估结论。 */
    private String assessment;
    /** 创建时间。 */
    private LocalDateTime createdAt;
}
