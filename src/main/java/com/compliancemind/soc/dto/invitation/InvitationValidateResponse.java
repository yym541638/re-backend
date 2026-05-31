package com.compliancemind.soc.dto.invitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class InvitationValidateResponse {

    private boolean valid;
    private String code;
    @JsonProperty("company_id")
    private Integer companyId;
    @JsonProperty("company_name")
    private String companyName;
    @JsonProperty("project_id")
    private Long projectId;
    @JsonProperty("project_name")
    private String projectName;
    @JsonProperty("member_role")
    private String memberRole;
    private String status;
    private String message;
}

