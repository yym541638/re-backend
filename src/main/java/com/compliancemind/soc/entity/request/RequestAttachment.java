package com.compliancemind.soc.entity.request;

import lombok.Data;

import java.time.LocalDateTime;

/** 请求附件元数据（{@code soc_request_attachment}）。 */
@Data
public class RequestAttachment {

    /** 附件 ID。 */
    private Long attachmentId;
    /** 所属请求 ID。 */
    private Long requestId;
    /** 原始文件名。 */
    private String fileName;
    /** 存储路径。 */
    private String filePath;
    /** 文件类型（如 pdf、excel、word）。 */
    private String fileType;
    /** MIME 类型。 */
    private String contentType;
    /** 文件大小（字节）。 */
    private Long fileSize;
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
