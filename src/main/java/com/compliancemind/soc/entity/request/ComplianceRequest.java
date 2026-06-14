package com.compliancemind.soc.entity.request;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** 合规请求 Individual（{@code soc_request}）。 */
@Data
public class ComplianceRequest {

    private Long requestId;
    private Long projectId;
    /** 所属 Request Master ID。 */
    private Long requestMasterId;
    private String requestCode;
    private String ccCriteria;
    private String title;
    private String requestDescription;
    private String pointsOfFocus;
    private String documentStatus;
    /** Upload Evidence Manual Status。 */
    private String evidenceManualStatus;
    private String documentOwner;
    private String requestAssignee;
    private Integer documentOwnerUserId;
    private LocalDate implementationDate;
    private LocalDateTime lastUpdateAt;
    private LocalDateTime requestSendDate;
    /** AI 审核状态：PENDING / RED / YELLOW / GREEN。 */
    private String aiReviewStatus;
    private String aiReviewComment;
    /** 用户手动 CommentContent。 */
    private String userComment;
    private String notes;
    private String requestor;
    private String comments;
    private String currentVersion;
    private Integer deleted;
    private Integer createdBy;
    private Integer updatedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
