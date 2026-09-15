package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** Request Master 页面 Individual 列表行。 */
@Data
public class RequestIndividualListItem {

    @JsonProperty("request_id")
    private Long requestId;

    @JsonProperty("request_code")
    private String requestCode;

    @JsonProperty("request_name")
    private String requestName;

    @JsonProperty("cc_criteria")
    private String ccCriteria;

    @JsonProperty("points_of_focus")
    private String pointsOfFocus;

    @JsonProperty("request_description")
    private String requestDescription;

    @JsonProperty("request_creation_date")
    private LocalDateTime requestCreationDate;

    @JsonProperty("request_assignee")
    private String requestAssignee;

    @JsonProperty("document_owner_name")
    private String documentOwnerName;

    @JsonProperty("upload_evidence")
    private String uploadEvidence;

    /** Evidence files for clickable view in list. */
    @JsonProperty("evidences")
    private List<RequestEvidenceItem> evidences;

    @JsonProperty("upload_evidence_date_time")
    private LocalDateTime uploadEvidenceDateTime;

    @JsonProperty("comment_content")
    private String commentContent;

    @JsonProperty("upload_evidence_manual_status")
    private String uploadEvidenceManualStatus;

    @JsonProperty("request_send_date")
    private LocalDateTime requestSendDate;

    /**
     * Review AI 列：not right / need attention / all good（red/yellow/green 映射）。
     */
    @JsonProperty("request_evidence_review_ai")
    @JsonAlias({"review_ai", "request_individual_review_status"})
    private String requestEvidenceReviewAi;

    /** 兼容旧字段名，值与 request_evidence_review_ai 相同。 */
    @JsonProperty("request_individual_review_status")
    private String requestIndividualReviewStatus;

    @JsonProperty("request_individual_review_comment")
    private String requestIndividualReviewComment;
}
