package com.compliancemind.soc.controller.compat;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.dto.auth.UpdateSystemRoleRequest;
import com.compliancemind.soc.dto.commerce.ProfileResponse;
import com.compliancemind.soc.dto.commerce.ProfileUpdateRequest;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.RoleCodes;
import com.compliancemind.soc.service.commerce.ProfileService;
import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 兼容旧版前端路径 {@code /user/**}：资料、成员检索、系统角色变更等。
 */
@RestController
@RequestMapping("/user")
public class LegacyUserController {

    private final ProfileService profileService;
    private final UserAccountMapper userAccountMapper;
    private final AuthorizationService authorizationService;

    public LegacyUserController(ProfileService profileService,
                                UserAccountMapper userAccountMapper,
                                AuthorizationService authorizationService) {
        this.profileService = profileService;
        this.userAccountMapper = userAccountMapper;
        this.authorizationService = authorizationService;
    }

    /**
     * 读取个人资料（旧版兼容路径）。
     *
     * <p>GET /user/info，需 JWT；等价于 GET /profile/me。</p>
     */
    @GetMapping("/info")
    public ApiResponse<ProfileResponse> info() {
        return ApiResponse.success(profileService.me());
    }

    /**
     * 更新个人资料（旧版兼容路径）。
     *
     * <p>PUT /user/info，需 JWT；等价于 PUT /profile/me。</p>
     */
    @PutMapping("/info")
    public ApiResponse<ProfileResponse> updateInfo(@Valid @RequestBody ProfileUpdateRequest request) {
        return ApiResponse.success(profileService.updateMe(request));
    }

    /**
     * 系统用户列表（System Users）。
     *
     * <p>GET /user/list，需 JWT 且为系统管理员（COMP_ADMIN）；返回全部用户（跨公司）。</p>
     */
    @GetMapping("/list")
    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(value = "keyword", required = false) String keyword) {
        authorizationService.requireCompanyManagement();
        List<UserAccount> users = userAccountMapper.listAllUsers(keyword);
        List<Map<String, Object>> list = users.stream().map(user -> {
            String role = RoleCodes.normalizeCompanyRole(user.getRoleCode());
            String systemRole = RoleCodes.toSystemRole(role);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("user_id", user.getUserId());
            item.put("username", user.getDisplayName());
            item.put("email", user.getEmail());
            item.put("phone", user.getPhone());
            item.put("company_id", user.getCompanyId());
            item.put("company_name", user.getCompanyName() == null ? "" : user.getCompanyName());
            item.put("role", role);
            item.put("system_role", systemRole);
            item.put("user_type", user.getUserType());
            item.put("status", user.getStatus());
            return item;
        }).toList();
        return ApiResponse.success(list);
    }

    /**
     * 变更用户系统角色（System Users）。
     *
     * <p>PUT /user/{id}/system-role，需 JWT 且为系统管理员；仅支持 COMP_ADMIN / COMP_USER。</p>
     */
    @PutMapping("/{id}/system-role")
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<Map<String, Object>> updateSystemRole(@PathVariable("id") Integer userId,
                                                             @Valid @RequestBody UpdateSystemRoleRequest request) {
        authorizationService.requireCompanyManagement();
        UserAccount target = userAccountMapper.selectById(userId);
        if (target == null) {
            throw new BizException(BizErrorCode.AUTH_USER_NOT_FOUND);
        }
        Integer companyId = target.getCompanyId();
        String systemRole = RoleCodes.normalizeSystemRole(request.getSystemRole());
        if (!RoleCodes.isSystemRole(systemRole)) {
            throw new BizException(BizErrorCode.AUTH_UNSUPPORTED_USER_ROLE);
        }
        String currentSystemRole = RoleCodes.toSystemRole(target.getRoleCode());
        if (RoleCodes.COMPANY_ADMIN.equals(systemRole) && !RoleCodes.COMPANY_ADMIN.equals(currentSystemRole)) {
            if (userAccountMapper.countByCompanyIdAndRoleCode(companyId, RoleCodes.COMPANY_ADMIN) > 0) {
                throw new BizException(BizErrorCode.AUTH_COMPANY_ADMIN_EXISTS);
            }
        }
        if (RoleCodes.COMPANY_USER.equals(systemRole) && RoleCodes.COMPANY_ADMIN.equals(currentSystemRole)) {
            if (userAccountMapper.countByCompanyIdAndRoleCode(companyId, RoleCodes.COMPANY_ADMIN) <= 1) {
                throw new BizException(BizErrorCode.AUTH_LAST_COMPANY_ADMIN);
            }
        }
        userAccountMapper.updateRoleCodeByUserId(userId, systemRole);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("user_id", userId);
        result.put("company_id", companyId);
        result.put("system_role", systemRole);
        result.put("role", systemRole);
        return ApiResponse.success(result);
    }

    /**
     * 角色下拉列表（PRD 2.5.12）。
     *
     * <p>GET /user/roles，需 JWT；公司管理员维护项目成员时选择角色。</p>
     */
    @GetMapping("/roles")
    public ApiResponse<List<Map<String, String>>> roles() {
        return ApiResponse.success(List.of(
            Map.of("roleCode", RoleCodes.COMPANY_ADMIN, "roleName", "Comp Admin"),
            Map.of("roleCode", RoleCodes.COMPANY_USER, "roleName", "Comp User"),
            Map.of("roleCode", RoleCodes.DOCUMENT_OWNER, "roleName", "Document owner"),
            Map.of("roleCode", RoleCodes.GENERAL_USER, "roleName", "General User"),
            Map.of("roleCode", RoleCodes.MANAGER, "roleName", "Manager"),
            Map.of("roleCode", RoleCodes.MANAGER_2, "roleName", "Manager 2"),
            Map.of("roleCode", RoleCodes.PROJECT_OWNER, "roleName", "Project owner")
        ));
    }
}
