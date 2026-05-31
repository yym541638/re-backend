package com.compliancemind.soc.controller.invitation;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.dto.invitation.InvitationCreateRequest;

import com.compliancemind.soc.dto.invitation.InvitationQueryRequest;

import com.compliancemind.soc.dto.invitation.InvitationValidateResponse;

import com.compliancemind.soc.entity.invitation.InvitationCode;

import com.compliancemind.soc.service.invitation.InvitationCodeService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;



import java.util.List;



/**

 * 邀请码：项目邀请创建、列表、校验（注册前置）、撤销。

 * <p>对应 PRD 2.5.2 Request Master、2.5.12 Project Settings、2.1.3 注册邀请码校验。</p>

 */

@RestController

@RequestMapping("/invitation-code")

public class InvitationCodeController {



    private final InvitationCodeService invitationCodeService;



    public InvitationCodeController(InvitationCodeService invitationCodeService) {

        this.invitationCodeService = invitationCodeService;

    }



    /**

     * 创建项目邀请码（PRD 2.5.2 / 2.5.12）。

     * <p>POST /invitation-code/project/create，需 JWT；指定项目、成员角色及有效期。</p>

     *

     * @param request 项目 ID、成员角色、最大使用次数、过期时间等

     * @return 新建的邀请码记录

     */

    @PostMapping("/project/create")

    public ApiResponse<InvitationCode> createProjectInvitation(@Valid @RequestBody InvitationCreateRequest request) {

        return ApiResponse.success(invitationCodeService.createProjectInvitation(request));

    }



    /**

     * 邀请码列表（PRD 2.5.2 / 2.5.12）。

     * <p>GET /invitation-code/list，需 JWT；可按 projectId、status 筛选。</p>

     *

     * @param projectId       项目 ID（camelCase，可选）

     * @param projectIdLegacy 项目 ID（snake_case 别名）

     * @param status          邀请码状态（可选）

     * @return 邀请码列表

     */

    @GetMapping("/list")

    public ApiResponse<List<InvitationCode>> list(@RequestParam(value = "projectId", required = false) Long projectId,

                                                  @RequestParam(value = "project_id", required = false) Long projectIdLegacy,

                                                  @RequestParam(value = "status", required = false) String status) {

        InvitationQueryRequest request = new InvitationQueryRequest();

        request.setProjectId(projectId != null ? projectId : projectIdLegacy);

        request.setStatus(status);

        return ApiResponse.success(invitationCodeService.list(request));

    }



    /**

     * 校验邀请码（PRD 2.1.3 注册）。

     * <p>GET /invitation-code/validate?code=xxx，匿名访问；注册前校验邀请码有效性。</p>

     *

     * @param code 邀请码字符串

     * @return 校验结果及关联项目/角色信息

     */

    @GetMapping("/validate")

    public ApiResponse<InvitationValidateResponse> validate(@RequestParam("code") String code) {

        return ApiResponse.success(invitationCodeService.validate(code));

    }



    /**

     * 撤销邀请码（PRD 2.5.12）。

     * <p>POST /invitation-code/revoke/{invitationId}，需 JWT；仅公司管理员可操作。</p>

     *

     * @param invitationId 邀请码记录 ID

     * @return 操作成功空响应

     */

    @PostMapping("/revoke/{invitationId}")

    public ApiResponse<Void> revoke(@PathVariable("invitationId") Long invitationId) {

        invitationCodeService.revoke(invitationId);

        return ApiResponse.success();

    }

}


