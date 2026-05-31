package com.compliancemind.soc.dto.request;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RequestUpdateRequest {

    @NotBlank(message = "CC Criteria 不能为空")
    @JsonAlias({"cc_criteria", "type"})
    private String ccCriteria;

    @NotBlank(message = "请求标题不能为空")
    @JsonAlias({"title", "name"})
    private String title;

    @JsonAlias({"request_description", "description"})
    private String requestDescription;
    @JsonAlias({"points_of_focus"})
    private String pointsOfFocus;
    @JsonAlias({"document_status"})
    private String documentStatus;
    @JsonAlias({"document_owner"})
    private String documentOwner;
    @JsonAlias({"implementation_date", "date_of_implementation"})
    private LocalDate implementationDate;
    private String notes;
    private String requestor;
    private String comments;
    @JsonAlias({"change_summary"})
    private String changeSummary;
}
