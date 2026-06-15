package com.compliancemind.soc.entity.project;

import lombok.Data;

import java.time.LocalDateTime;

/** SOC 合规项目（{@code soc_project}）。 */
@Data
public class Project {

    /** 项目 ID。 */
    private Long projectId;
    /** 所属公司 ID。 */
    private Integer companyId;
    /** 项目名称。 */
    private String projectName;
    /** 项目描述（Project Info）。 */
    private String projectInfo;
    /** 项目开始时间。 */
    private LocalDateTime startDate;
    /** 项目结束时间。 */
    private LocalDateTime endDate;
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
