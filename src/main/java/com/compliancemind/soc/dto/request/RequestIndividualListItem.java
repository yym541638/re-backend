package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

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

    @JsonProperty("upload_evidence_date_time")
    private LocalDateTime uploadEvidenceDateTime;

    @JsonProperty("comment_content")
    private String commentContent;

    @JsonProperty("upload_evidence_manual_status")
    private String uploadEvidenceManualStatus;

    @JsonProperty("request_send_date")
    private LocalDateTime requestSendDate;

    @JsonProperty("request_individual_review_status")
    private String requestIndividualReviewStatus;

    @JsonProperty("request_individual_review_comment")
    private String requestIndividualReviewComment;
}
