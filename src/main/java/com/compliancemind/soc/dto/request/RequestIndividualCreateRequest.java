package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RequestIndividualCreateRequest {

    @NotNull(message = "Request Master 不能为空")
    @JsonAlias({"request_master_id"})
    private Long requestMasterId;

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
}
