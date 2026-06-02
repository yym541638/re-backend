package com.compliancemind.soc.entity.project;

import lombok.Data;

import java.time.LocalDateTime;

/** 项目创建/概览附件（{@code soc_project_attachment}）。 */
@Data
public class ProjectAttachment {

    private Long attachmentId;
    private Long projectId;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private Integer deleted;
    private Integer createdBy;
    private LocalDateTime createdAt;
}
