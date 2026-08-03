package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Request Individual 侧栏表单。 */
@Data
public class RequestIndividualDetailResponse {

    @JsonProperty("request_id")
    private Long requestId;

    @JsonProperty("request_master_id")
    private Long requestMasterId;

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("request_code")
    private String requestCode;

    @JsonProperty("request_name")
    @JsonAlias({"title", "name"})
    private String requestName;

    @JsonProperty("cc_criteria")
    private String ccCriteria;

    @JsonProperty("points_of_focus")
    private String pointsOfFocus;

    @JsonProperty("request_description")
    private String requestDescription;

    @JsonProperty("request_creation_date")
    private LocalDateTime requestCreationDate;

    @JsonProperty("document_owner_name")
    private String documentOwnerName;

    @JsonProperty("document_owner_user_id")
    private Integer documentOwnerUserId;

    @JsonProperty("request_assignee")
    private String requestAssignee;

    @JsonProperty("upload_evidence_manual_status")
    private String uploadEvidenceManualStatus;

    @JsonProperty("request_send_date")
    private LocalDateTime requestSendDate;

    /**
     * Request Evidence Review AI：send 后回填，not right / need attention / all good。
     */
    @JsonProperty("request_evidence_review_ai")
    @JsonAlias({"requestEvidenceReviewAi", "request_evidence_review_ai_status"})
    private String requestEvidenceReviewAi;

    /** AI 颜色码：red / yellow / green。 */
    @JsonProperty("request_evidence_review_ai_color")
    private String requestEvidenceReviewAiColor;

    /** 兼容旧字段名，值与 request_evidence_review_ai 相同。 */
    @JsonProperty("request_evidence_review_ai_status")
    private String requestEvidenceReviewAiStatus;

    @JsonProperty("ai_comment_content")
    private String aiCommentContent;

    @JsonProperty("comment_content")
    private String commentContent;

    private List<RequestEvidenceItem> evidences;
}
