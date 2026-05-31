package com.compliancemind.soc.controller.compat;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.dto.commerce.CompanyProfileResponse;

import com.compliancemind.soc.service.commerce.ProfileService;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;



/**

 * 兼容旧版 {@code /admin/company} 公司资料读取。

 * <p>等价于 GET /profile/company（PRD 2.4）。</p>

 */

@RestController

@RequestMapping("/admin")

public class LegacyAdminController {



    private final ProfileService profileService;



    public LegacyAdminController(ProfileService profileService) {

        this.profileService = profileService;

    }



    /**

     * 读取公司资料（旧版兼容路径）。

     * <p>GET /admin/company，需 JWT；等价于 GET /profile/company。</p>

     *

     * @return 当前用户所属公司资料

     */

    @GetMapping("/company")

    public ApiResponse<CompanyProfileResponse> company() {

        return ApiResponse.success(profileService.company());

    }

}


