package com.compliancemind.soc.dto.commerce;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ProfileResponse {

    @JsonProperty("user_id")
    private Integer userId;
    @JsonProperty("username")
    private String displayName;
    private String email;
    private String phone;
    @JsonProperty("avatar_url")
    private String avatarUrl;
    @JsonProperty("job_title")
    private String jobTitle;
    @JsonProperty("role")
    private String roleCode;
    private CompanyProfileResponse company;
}
