package com.compliancemind.soc.entity.rcm;

import lombok.Data;

import java.time.LocalDateTime;

/** RCM 控制矩阵主记录（{@code soc_rcm}）。 */
@Data
public class RcmRecord {

    /** RCM 记录 ID。 */
    private Long rcmId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 控制编号。 */
    private String controlCode;
    /** 控制名称。 */
    private String controlName;
    /** 控制描述。 */
    private String description;
    /** 分类/模块（CC 标准）。 */
    private String category;
    /** 模块名称。 */
    private String moduleName;
    /** 风险描述。 */
    private String riskDescription;
    /** 控制状态（如 PENDING）。 */
    private String status;
    /** 所处阶段（MANUAL / FINAL / AI_GENERATED）。 */
    private String stage;
    /** 是否由 AI 生成。 */
    private Boolean aiGenerated;
    /** 来源 Request ID。 */
    private Long sourceRequestId;
    /** 来源 RCM ID（晋级/复制时）。 */
    private Long sourceRcmId;
    /** 控制目标。 */
    private String controlObjective;
    /** 实施方法。 */
    private String implementationMethod;
    /** 证据要求。 */
    private String evidenceRequirement;
    /** 控制执行人。 */
    private String controlPerformer;
    /** 控制审核人。 */
    private String controlReviewer;
    /** 附加负责人。 */
    private String additionalOwner;
    /** 控制风险等级。 */
    private String controlRiskRating;
    /** 当前版本号（如 V1）。 */
    private String currentVersion;
    /** 软删除标记（0=未删除，1=已删除）。 */
    private Integer deleted;
    /** 创建人用户 ID。 */
    private Integer createdBy;
    /** 最后更新人用户 ID。 */
    private Integer updatedBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
