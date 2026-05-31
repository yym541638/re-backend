package com.compliancemind.soc.entity.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 合规请求主数据（{@code soc_request}）。 */
@Data
public class ComplianceRequest {

    /** 请求 ID。 */
    private Long requestId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 请求编码（唯一）。 */
    private String requestCode;
    /** CC 标准/准则（Common Criteria）。 */
    private String ccCriteria;
    /** 请求标题。 */
    private String title;
    /** 请求描述。 */
    private String requestDescription;
    /** 关注要点（Points of Focus）。 */
    private String pointsOfFocus;
    /** 文档状态（如 PENDING）。 */
    private String documentStatus;
    /** 文档负责人。 */
    private String documentOwner;
    /** 实施日期。 */
    private LocalDate implementationDate;
    /** 最后更新时间（业务字段）。 */
    private LocalDateTime lastUpdateAt;
    /** 备注。 */
    private String notes;
    /** 请求发起人。 */
    private String requestor;
    /** 评论/说明。 */
    private String comments;
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
