package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RequestIndividualUpdateRequest {

    @NotBlank(message = "Request Name 不能为空")
    @JsonAlias({"request_name", "title", "name"})
    private String requestName;

    @JsonAlias({"cc_criteria", "type"})
    private String ccCriteria;

    @JsonAlias({"points_of_focus"})
    private String pointsOfFocus;

    @JsonAlias({"request_description", "description"})
    private String requestDescription;

    @JsonAlias({"document_owner_name", "document_owner"})
    private String documentOwnerName;

    @JsonAlias({"document_owner_user_id"})
    private Integer documentOwnerUserId;

    @JsonAlias({"request_assignee"})
    private String requestAssignee;

    @JsonAlias({"comment_content", "user_comment"})
    private String commentContent;

    @JsonAlias({"upload_evidence_manual_status", "evidence_manual_status"})
    private String uploadEvidenceManualStatus;

    /** Manual / AI review status: RED / YELLOW / GREEN / PENDING，或 not right / need attention / all good。 */
    @JsonAlias({
        "ai_review_status",
        "request_evidence_review_ai",
        "request_evidence_review_ai_status",
        "request_individual_review_status",
        "review_ai_status"
    })
    private String aiReviewStatus;

    @JsonAlias({
        "ai_review_comment",
        "ai_comment_content",
        "request_individual_review_comment",
        "review_comment"
    })
    private String aiReviewComment;
}
