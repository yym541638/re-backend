package com.compliancemind.soc.dto.auth;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LoginResponse {

    private String token;
    @JsonProperty("expire_in")
    private long expireSeconds;
    @JsonProperty("purchase_status")
    private Integer purchaseStatus;
    @JsonProperty("redirect_to")
    private String redirectTo;
    @JsonProperty("user_info")
    private UserInfo user;

    @Data
    public static class UserInfo {
        @JsonProperty("id")
        private Integer userId;
        @JsonProperty("company_id")
        private Integer companyId;
        @JsonProperty("company_name")
        private String companyName;
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
    }
}
