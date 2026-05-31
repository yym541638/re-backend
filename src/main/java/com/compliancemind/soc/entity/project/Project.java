package com.compliancemind.soc.entity.project;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** SOC 合规项目（{@code soc_project}）。 */
@Data
public class Project {

    /** 项目 ID。 */
    private Long projectId;
    /** 所属公司 ID。 */
    private Integer companyId;
    /** 项目编码（唯一）。 */
    private String projectCode;
    /** 项目名称。 */
    private String projectName;
    /** 合规类型（如 SOC2、ISO27001）。 */
    private String complianceType;
    /** 审计类型（Type1 / Type2）。 */
    private String auditType;
    /** 当前版本号（如 V1）。 */
    private String currentVersion;
    /** 差距分析条目数量。 */
    private Integer gapCount;
    /** 项目状态（如 IN_PROGRESS）。 */
    private String status;
    /** 项目开始日期。 */
    private LocalDate startDate;
    /** 项目结束日期。 */
    private LocalDate endDate;
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
