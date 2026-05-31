package com.compliancemind.soc.entity.analysis;

import lombok.Data;

import java.time.LocalDateTime;

/** 差距分析条目（{@code soc_gap_analysis}）。 */
@Data
public class GapAnalysisRecord {

    /** 差距记录 ID。 */
    private Long gapId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 来源控制测试 ID。 */
    private Long sourceTestId;
    /** 控制标题。 */
    private String controlTitle;
    /** 差距等级。 */
    private String gapLevel;
    /** 差距状态（如 OPEN）。 */
    private String status;
    /** 差距描述。 */
    private String gapDescription;
    /** 整改建议。 */
    private String remediationSuggestion;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
