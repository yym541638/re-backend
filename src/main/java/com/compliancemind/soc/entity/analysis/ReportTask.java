package com.compliancemind.soc.entity.analysis;

import lombok.Data;

import java.time.LocalDateTime;

/** 异步报告生成任务（{@code soc_report_task}）。 */
@Data
public class ReportTask {

    /** 任务 ID。 */
    private Long taskId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 报告类型。 */
    private String reportType;
    /** 导出格式（如 PDF、XLSX）。 */
    private String format;
    /** 包含章节配置（JSON）。 */
    private String includeSectionsJson;
    /** 报告语言。 */
    private String language;
    /** 任务状态（如 PENDING、RUNNING、COMPLETED、FAILED）。 */
    private String status;
    /** 生成进度（0~100）。 */
    private Integer progress;
    /** 生成文件存储路径。 */
    private String filePath;
    /** 失败错误信息。 */
    private String errorMessage;
    /** 创建人用户 ID。 */
    private Integer createdBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
