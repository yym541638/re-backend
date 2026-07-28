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
        /** 用户类型：CLIENT / CONSULTANT / AUDITOR。 */
        @JsonProperty("user_type")
        private String userType;
        /** 公司权限编码（兼容旧前端）。 */
        @JsonProperty("role")
        private String roleCode;
        /**
         * 系统角色：{@code COMP_ADMIN} / {@code COMP_USER}。
         * <p>前端双层权限入口控制字段；兼容旧字段时可回退 {@link #permissionCode}=administrator。</p>
         */
        @JsonProperty("system_role")
        private String systemRole;
        /**
         * 旧版权限文案兼容：管理员为 {@code administrator}，普通用户为 {@code user}。
         */
        @JsonProperty("permissionCode")
        private String permissionCode;
    }
}
