package com.compliancemind.soc.controller.commerce;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.dto.commerce.CompanyProfileResponse;

import com.compliancemind.soc.dto.commerce.CompanyProfileUpdateRequest;

import com.compliancemind.soc.dto.commerce.ProfileResponse;

import com.compliancemind.soc.dto.commerce.ProfileUpdateRequest;

import com.compliancemind.soc.service.commerce.ProfileService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;



/**

 * 用户与公司资料（新版路径 {@code /profile}）。

 * <p>对应 PRD 2.4 Profile 个人/公司资料。</p>

 */

@RestController

@RequestMapping("/profile")

public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {

        this.profileService = profileService;

    }

    /**

     * 读取个人资料（PRD 2.4）。

     * <p>GET /profile/me，需 JWT；全部用户可访问。</p>

     *

     * @return 当前用户个人资料

     */

    @GetMapping("/me")

    public ApiResponse<ProfileResponse> me() {

        return ApiResponse.success(profileService.me());

    }

    /**

     * 更新个人资料（PRD 2.4）。

     * <p>PUT /profile/me，需 JWT；可更新显示名、邮箱、手机、头像、职位。</p>

     * @param request 个人资料更新字段

     * @return 更新后的个人资料

     */

    @PutMapping("/me")

    public ApiResponse<ProfileResponse> updateMe(@Valid @RequestBody ProfileUpdateRequest request) {

        return ApiResponse.success(profileService.updateMe(request));

    }



    /**

     * 读取公司资料（PRD 2.4）。

     * <p>GET /profile/company，需 JWT；仅公司管理员可访问。</p>

     *

     * @return 当前用户所属公司资料

     */

    @GetMapping("/company")

    public ApiResponse<CompanyProfileResponse> company() {

        return ApiResponse.success(profileService.company());

    }



    /**

     * 更新公司资料（PRD 2.4）。

     * <p>PUT /profile/company，需 JWT；仅公司管理员可访问。</p>

     *

     * @param request 公司资料更新字段

     * @return 更新后的公司资料

     */

    @PutMapping("/company")

    public ApiResponse<CompanyProfileResponse> updateCompany(@Valid @RequestBody CompanyProfileUpdateRequest request) {

        return ApiResponse.success(profileService.updateCompany(request));

    }

}


