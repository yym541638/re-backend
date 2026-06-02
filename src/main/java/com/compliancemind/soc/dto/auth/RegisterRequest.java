package com.compliancemind.soc.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RegisterRequest {

    private String displayName;

    @JsonAlias({"firstName"})
    private String firstName;

    @JsonAlias({"lastName"})
    private String lastName;

    @Email(message = "邮箱格式不正确")
    @NotBlank(message = "邮箱不能为空")
    private String email;

    @NotBlank(message = "手机号不能为空")
    private String phone;

    @JsonAlias({"companyName"})
    @NotBlank(message = "公司名称不能为空")
    private String companyName;

    /**
     * 用户类型（注册页 Role 下拉）：Clients / Consultant / Auditor。
     */
    @JsonAlias({"user_type", "userType"})
    private String userType;

    /**
     * 公司权限（注册页 Permissions 下拉）：Admin / Document Owner / General User / Manager tier1 / Manager tier2。
     */
    @JsonAlias({"permissions", "permission", "permissionCode"})
    private String permissionCode;

    /**
     * 兼容旧版请求：曾用 role / roleCode 传权限编码。
     */
    @JsonAlias({"role", "roleCode"})
    private String roleCode;

    @JsonAlias({"invitationCode", "invitation_code"})
    private String invitationCode;

    @Size(min = 6, max = 64, message = "密码长度需在 6 到 64 位之间")
    @NotBlank(message = "密码不能为空")
    private String password;
}
