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
     * 公司权限（注册页 Permissions 下拉）：Admin / Document Owner / General User / Manager tier1 / Manager tier2。
     * <p>推荐传编码：{@code COMP_ADMIN} / {@code DOCUMENT_OWNER} / {@code GENERAL_USER} / {@code MANAGER} / {@code MANAGER_2}。
     * 也兼容 UI 文案，如 {@code administrator}。</p>
     */
    @JsonAlias({"permissions", "permission", "permissionCode"})
    private String permissionCode;

    /**
     * 用户类型（注册页 User Type）：Clients / Consultant / Auditor。
     * <p>存库字段 {@code user_type}，编码：{@code CLIENT} / {@code CONSULTANT} / {@code AUDITOR}。</p>
     */
    @JsonAlias({"userType", "user_type"})
    private String userType;

    /**
     * 兼容字段：可为权限编码，也可为用户类型（如 {@code clients}）。
     * <p>当 {@link #permissionCode} / {@link #userType} 已分别传入时，本字段可省略。</p>
     */
    @JsonAlias({"role", "roleCode"})
    private String roleCode;

    @JsonAlias({"invitationCode", "invitation_code"})
    private String invitationCode;

    @Size(min = 6, max = 64, message = "密码长度需在 6 到 64 位之间")
    @NotBlank(message = "密码不能为空")
    private String password;
}
