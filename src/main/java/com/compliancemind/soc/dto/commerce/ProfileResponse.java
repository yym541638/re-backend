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
    /** 系统角色：COMP_ADMIN / COMP_USER。 */
    @JsonProperty("system_role")
    private String systemRole;
    /** 用户类型：CLIENT / CONSULTANT / AUDITOR。 */
    @JsonProperty("user_type")
    private String userType;
    /** 旧前端兼容：administrator / user。 */
    @JsonProperty("permissionCode")
    private String permissionCode;
    private CompanyProfileResponse company;
}
