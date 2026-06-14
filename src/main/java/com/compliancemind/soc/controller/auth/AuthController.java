package com.compliancemind.soc.controller.auth;


import com.compliancemind.soc.dto.auth.LoginRequest;

import com.compliancemind.soc.dto.auth.LoginResponse;

import com.compliancemind.soc.dto.auth.RegisterRequest;

import com.compliancemind.soc.dto.commerce.CompanyProfileResponse;

import com.compliancemind.soc.service.auth.AuthService;

import com.compliancemind.soc.common.api.ApiResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;


/**
 * 认证相关接口：登录、注册及当前用户信息（JWT）。
 *
 * <p>对应 PRD 2.1.2 登录、2.1.3 注册。</p>
 */

@RestController

@RequestMapping("/auth")

public class AuthController {


    private final AuthService authService;


    public AuthController(AuthService authService) {

        this.authService = authService;

    }


    /**
     * 用户登录（PRD 2.1.2）。
     *
     * <p>POST /auth/login，匿名访问；支持邮箱或手机号 + 密码登录。</p>
     *
     * @param request 登录凭据（account、password）
     * @return JWT token、过期时间、购买状态及跳转信息
     */

    @PostMapping("/login")

    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {

        return ApiResponse.success(authService.login(request));

    }


    /**
     * 用户注册（PRD 2.1.3）。
     *
     * <p>POST /auth/register，匿名访问；可选填写邀请码。</p>
     *
     * @param request 注册信息（邮箱、手机、公司、permissions、密码等；可选邀请码）
     * @return 注册成功后返回 JWT 及用户信息 包括购买信息
     */

    @PostMapping("/register")

    public ApiResponse<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {

        return ApiResponse.success(authService.register(request));

    }


    /**
     * 获取当前登录用户信息（PRD 2.1.3）。
     *
     * <p>GET /auth/me，需 JWT 认证；用于登录态校验。</p>
     *
     * @return 当前用户概要及 token 信息
     */

    @GetMapping("/me")

    public ApiResponse<LoginResponse> me() {

        return ApiResponse.success(authService.me());

    }


    /**
     * 根据邀请码查询关联公司完整信息（PRD 2.1.3 注册）。
     *
     * <p>GET /auth/company-by-invitation?code=xxx，匿名访问；用于注册页展示公司资料。</p>
     *
     * @param code           邀请码（camelCase）
     * @param invitationCode 邀请码（snake_case 别名）
     * @return 公司完整资料
     */
    @GetMapping("/company-by-invitation")
    public ApiResponse<CompanyProfileResponse> companyByInvitation(
        @RequestParam(value = "code", required = false) String code,
        @RequestParam(value = "invitation_code", required = false) String invitationCode) {
        String resolvedCode = code != null ? code : invitationCode;
        return ApiResponse.success(authService.getCompanyByInvitationCode(resolvedCode));
    }

}


