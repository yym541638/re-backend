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

    /**
     * 公司名称。
     * <p>无邀请码时必填；有邀请码时由邀请码绑定公司决定，可省略。</p>
     */
    @JsonAlias({"companyName"})
    private String companyName;

    /**
     * 系统角色（注册页 Permissions）：Admin / Comp User。
     * <p>推荐传编码：{@code COMP_ADMIN} / {@code COMP_USER}。
     * 也兼容 UI 文案，如 {@code administrator}；未传时：有邀请码默认 COMP_USER，无邀请码默认 COMP_ADMIN。</p>
     */
    @JsonAlias({"permissions", "permission", "permissionCode", "systemRole", "system_role"})
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
