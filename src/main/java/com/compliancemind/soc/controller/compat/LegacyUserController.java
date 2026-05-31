package com.compliancemind.soc.controller.compat;



import com.compliancemind.soc.entity.auth.UserAccount;

import com.compliancemind.soc.mapper.auth.UserAccountMapper;

import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.dto.commerce.ProfileResponse;

import com.compliancemind.soc.dto.commerce.ProfileUpdateRequest;

import com.compliancemind.soc.service.commerce.ProfileService;

import com.compliancemind.soc.security.AuthorizationService;

import com.compliancemind.soc.security.RoleCodes;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;



import java.util.List;

import java.util.Map;



/**

 * 兼容旧版前端路径 {@code /user/**}：资料与成员检索等。

 * <p>对应 PRD 2.5.12 Project Settings（用户搜索、角色下拉）。</p>

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

     * <p>GET /user/info，需 JWT；等价于 GET /profile/me。</p>

     *

     * @return 当前用户个人资料

     */

    @GetMapping("/info")

    public ApiResponse<ProfileResponse> info() {

        return ApiResponse.success(profileService.me());

    }



    /**

     * 更新个人资料（旧版兼容路径）。

     * <p>PUT /user/info，需 JWT；等价于 PUT /profile/me。</p>

     *

     * @param request 个人资料更新字段

     * @return 更新后的个人资料

     */

    @PutMapping("/info")

    public ApiResponse<ProfileResponse> updateInfo(@Valid @RequestBody ProfileUpdateRequest request) {

        return ApiResponse.success(profileService.updateMe(request));

    }



    /**

     * 本公司用户搜索（PRD 2.5.12）。

     * <p>GET /user/list，需 JWT；公司管理员按 keyword 搜索本公司用户，用于添加项目成员。</p>

     *

     * @param keyword 搜索关键字（可选，匹配姓名/邮箱/手机）

     * @return 用户列表（user_id、username、email、phone、role）

     */

    @GetMapping("/list")

    public ApiResponse<List<Map<String, Object>>> list(@RequestParam(value = "keyword", required = false) String keyword) {

        Integer companyId = authorizationService.currentCompanyId();

        List<UserAccount> users = userAccountMapper.listUsers(companyId, keyword);

        List<Map<String, Object>> list = users.stream().map(user -> Map.<String, Object>of(

            "user_id", user.getUserId(),

            "username", user.getDisplayName(),

            "email", user.getEmail(),

            "phone", user.getPhone(),

            "role", RoleCodes.normalizeCompanyRole(user.getRoleCode())

        )).toList();

        return ApiResponse.success(list);

    }



    /**

     * 角色下拉列表（PRD 2.5.12）。

     * <p>GET /user/roles，需 JWT；公司管理员维护项目成员时选择角色。</p>

     *

     * @return 可选角色列表（roleCode、roleName）

     */

    @GetMapping("/roles")

    public ApiResponse<List<Map<String, String>>> roles() {

        return ApiResponse.success(List.of(

            Map.of("roleCode", RoleCodes.COMPANY_ADMIN, "roleName", "Comp Admin"),

            Map.of("roleCode", RoleCodes.DOCUMENT_OWNER, "roleName", "Document owner"),

            Map.of("roleCode", RoleCodes.GENERAL_USER, "roleName", "General User"),

            Map.of("roleCode", RoleCodes.MANAGER, "roleName", "Manager"),

            Map.of("roleCode", RoleCodes.MANAGER_2, "roleName", "Manager 2"),

            Map.of("roleCode", RoleCodes.PROJECT_OWNER, "roleName", "Project owner")

        ));

    }

}


