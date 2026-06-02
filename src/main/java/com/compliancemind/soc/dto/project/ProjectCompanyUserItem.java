package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 项目成员选择弹窗用的公司用户摘要。
 */
@Data
public class ProjectCompanyUserItem {

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("username")
    private String displayName;

    private String email;

    private String phone;

    /** 公司级权限（Permissions）。 */
    @JsonProperty("permission")
    private String permissionCode;

    /** 用户类型（Clients / Consultant / Auditor）。 */
    @JsonProperty("user_type")
    private String userType;
}
