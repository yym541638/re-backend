package com.compliancemind.soc.dto.auth;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 变更用户系统角色：{@code COMP_ADMIN} / {@code COMP_USER}。
 */
@Data
public class UpdateSystemRoleRequest {

    @NotBlank(message = "系统角色不能为空")
    @JsonAlias({"systemRole", "system_role", "role", "roleCode", "permissionCode"})
    private String systemRole;
}
