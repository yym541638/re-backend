package com.compliancemind.soc.entity.request;

import lombok.Data;

import java.time.LocalDateTime;

/** Request Master 模板文件（{@code soc_request_master_template_file}）。 */
@Data
public class RequestMasterTemplateFile {

    private Long templateFileId;
    private Long requestMasterId;
    private Integer fileNo;
    private String fileName;
    private String filePath;
    private String relevantCriteria;
    private Integer deleted;
    private Integer createdBy;
    private Integer updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
