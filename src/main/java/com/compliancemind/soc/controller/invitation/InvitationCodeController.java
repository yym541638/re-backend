package com.compliancemind.soc.controller.invitation;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.dto.invitation.InvitationCreateRequest;
import com.compliancemind.soc.dto.invitation.InvitationQueryRequest;
import com.compliancemind.soc.dto.invitation.InvitationRedeemRequest;
import com.compliancemind.soc.dto.invitation.InvitationRedeemResponse;
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
 * 邀请码：项目邀请创建、列表、校验、登录态兑换、撤销。
 */
@RestController
@RequestMapping("/invitation-code")
public class InvitationCodeController {

    private final InvitationCodeService invitationCodeService;

    public InvitationCodeController(InvitationCodeService invitationCodeService) {
        this.invitationCodeService = invitationCodeService;
    }

    /**
     * 创建项目邀请码（Access Management「Invite」）。
     *
     * <p>POST /invitation-code/project/create，需 JWT；指定项目、成员角色及有效期。</p>
     */
    @PostMapping("/project/create")
    public ApiResponse<InvitationCode> createProjectInvitation(@Valid @RequestBody InvitationCreateRequest request) {
        return ApiResponse.success(invitationCodeService.createProjectInvitation(request));
    }

    /**
     * 邀请码列表。
     *
     * <p>GET /invitation-code/list，需 JWT；可按 projectId、status 筛选。</p>
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
     * 校验邀请码（注册前置，可匿名）。
     *
     * <p>GET /invitation-code/validate?code=xxx</p>
     */
    @GetMapping("/validate")
    public ApiResponse<InvitationValidateResponse> validate(@RequestParam("code") String code) {
        return ApiResponse.success(invitationCodeService.validate(code));
    }

    /**
     * 已登录用户兑换邀请码（Access Management「Invitation code」弹窗 Confirm）。
     *
     * <p>POST /invitation-code/redeem，需 JWT；将当前用户加入邀请码绑定项目的角色槽位。</p>
     */
    @PostMapping("/redeem")
    public ApiResponse<InvitationRedeemResponse> redeem(@Valid @RequestBody InvitationRedeemRequest request) {
        return ApiResponse.success(invitationCodeService.redeemForCurrentUser(request.getCode()));
    }

    /**
     * 撤销邀请码。
     *
     * <p>POST /invitation-code/revoke/{invitationId}，需 JWT。</p>
     */
    @PostMapping("/revoke/{invitationId}")
    public ApiResponse<Void> revoke(@PathVariable("invitationId") Long invitationId) {
        invitationCodeService.revoke(invitationId);
        return ApiResponse.success();
    }
}
