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

    @JsonAlias({"role"})
    @NotBlank(message = "角色不能为空")
    private String roleCode;

    @JsonAlias({"invitationCode", "invitation_code"})
    private String invitationCode;

    @Size(min = 6, max = 64, message = "密码长度需在 6 到 64 位之间")
    @NotBlank(message = "密码不能为空")
    private String password;
}
