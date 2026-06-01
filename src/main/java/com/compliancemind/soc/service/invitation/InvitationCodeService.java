package com.compliancemind.soc.service.invitation;

import com.compliancemind.soc.entity.auth.Company;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.CompanyMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.common.i18n.LocalizedMessageResolver;
import com.compliancemind.soc.dto.invitation.InvitationCreateRequest;
import com.compliancemind.soc.dto.invitation.InvitationQueryRequest;
import com.compliancemind.soc.dto.invitation.InvitationValidateResponse;
import com.compliancemind.soc.entity.invitation.InvitationCode;
import com.compliancemind.soc.mapper.invitation.InvitationCodeMapper;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.project.ProjectMember;
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.mapper.project.ProjectMemberMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.security.RoleCodes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目邀请码：生成、校验、列表、撤销与注册消费。
 */
@Service
public class InvitationCodeService {

    private final InvitationCodeMapper invitationCodeMapper;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final CompanyMapper companyMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;
    private final LocalizedMessageResolver localizedMessageResolver;

    public InvitationCodeService(InvitationCodeMapper invitationCodeMapper,
                                   ProjectMapper projectMapper,
                                   ProjectMemberMapper projectMemberMapper,
                                   CompanyMapper companyMapper,
                                   AuthorizationService authorizationService,
                                   CurrentUserAccessor currentUserAccessor,
                                   OperationLogService operationLogService,
                                   LocalizedMessageResolver localizedMessageResolver) {
        this.invitationCodeMapper = invitationCodeMapper;
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.companyMapper = companyMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
        this.localizedMessageResolver = localizedMessageResolver;
    }

    @Transactional(rollbackFor = Exception.class)
    public InvitationCode createProjectInvitation(InvitationCreateRequest request) {
        Project project = authorizationService.requireProjectManage(request.getProjectId());
        String memberRole = RoleCodes.normalizeProjectRole(request.getMemberRole());
        if (!RoleCodes.isProjectRole(memberRole)) {
            throw new BizException(BizErrorCode.PROJECT_MEMBER_ROLE_UNSUPPORTED);
        }
        InvitationCode invitationCode = new InvitationCode();
        invitationCode.setCode(buildProjectCode(project));
        invitationCode.setInvitationType(SocConstants.Invitation.TYPE_PROJECT);
        invitationCode.setCompanyId(project.getCompanyId());
        invitationCode.setProjectId(project.getProjectId());
        invitationCode.setMemberRole(memberRole);
        invitationCode.setStatus(SocConstants.Invitation.STATUS_ACTIVE);
        invitationCode.setMaxUses(request.getMaxUses() == null || request.getMaxUses() < 1
            ? SocConstants.Invitation.DEFAULT_MAX_USES
            : request.getMaxUses());
        invitationCode.setUsedCount(0);
        invitationCode.setExpiresAt(request.getExpiresAt());
        invitationCode.setRemark(request.getRemark());
        invitationCode.setCreatedBy(currentUserAccessor.requireUserId());
        invitationCodeMapper.insert(invitationCode);
        operationLogService.record(SocConstants.OperationLog.Module.INVITATION_CODE,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(project.getProjectId()),
            project.getProjectName(),
            project.getProjectId(),
            SocConstants.OperationLog.Detail.INVITE_CREATE_PREFIX_ZH + invitationCode.getCode());
        return invitationCode;
    }

    public List<InvitationCode> list(InvitationQueryRequest request) {
        if (request.getProjectId() == null) {
            authorizationService.requireCompanyProjectManagement();
        } else {
            authorizationService.requireProjectRead(request.getProjectId());
        }
        return invitationCodeMapper.list(request);
    }

    public InvitationValidateResponse validate(String code) {
        InvitationCode invitationCode = invitationCodeMapper.selectByCode(code);
        InvitationValidateResponse response = new InvitationValidateResponse();
        response.setCode(code);
        if (invitationCode == null) {
            response.setValid(false);
            response.setStatus(SocConstants.Invitation.RESPONSE_CODE_NOT_FOUND);
            response.setMessage(localizedMessageResolver.message(BizErrorCode.INVITATION_NOT_FOUND));
            return response;
        }
        response.setStatus(invitationCode.getStatus());
        response.setCompanyId(invitationCode.getCompanyId());
        Company company = companyMapper.selectById(invitationCode.getCompanyId());
        response.setCompanyName(company == null ? "" : company.getCompanyName());
        response.setProjectId(invitationCode.getProjectId());
        Project project = invitationCode.getProjectId() == null ? null : projectMapper.selectById(invitationCode.getProjectId());
        response.setProjectName(project == null ? "" : project.getProjectName());
        response.setMemberRole(invitationCode.getMemberRole());
        if (!isUsable(invitationCode)) {
            response.setValid(false);
            response.setMessage(localizedMessageResolver.message(unusableInvitationReason(invitationCode)));
            return response;
        }
        response.setValid(true);
        response.setMessage(localizedMessageResolver.message(SocConstants.MessageKeys.INVITATION_VALIDATE_AVAILABLE));
        return response;
    }

    public InvitationCode requireUsableCode(String code) {
        InvitationCode invitationCode = invitationCodeMapper.selectByCode(code);
        if (invitationCode == null) {
            throw new BizException(BizErrorCode.INVITATION_NOT_FOUND);
        }
        if (!isUsable(invitationCode)) {
            throw new BizException(unusableInvitationReason(invitationCode));
        }
        return invitationCode;
    }

    /**
     * 根据邀请码解析关联公司；不要求邀请码仍可用（注册页展示公司信息用）。
     */
    public Company resolveCompanyByCode(String code) {
        if (code == null || code.isBlank()) {
            throw new BizException(BizErrorCode.INVITATION_NOT_FOUND);
        }
        InvitationCode invitationCode = invitationCodeMapper.selectByCode(code.trim());
        if (invitationCode == null) {
            throw new BizException(BizErrorCode.INVITATION_NOT_FOUND);
        }
        Company company = companyMapper.selectById(invitationCode.getCompanyId());
        if (company == null) {
            throw new BizException(BizErrorCode.AUTH_INVITATION_COMPANY_MISSING);
        }
        return company;
    }

    /**
     * 注册成功后消费邀请码：更新使用次数/状态，并将用户加入邀请码关联的项目。
     * <p>由 {@link com.compliancemind.soc.service.auth.AuthService#register} 在携带有效邀请码时调用。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public void consumeForUser(InvitationCode invitationCode, UserAccount userAccount) {
        // 已使用次数 +1，记录本次注册消耗一次邀请额度
        invitationCode.setUsedCount(invitationCode.getUsedCount() + 1);
        // 记录实际使用该邀请码完成注册的用户 ID
        invitationCode.setUsedBy(userAccount.getUserId());
        // 记录邀请码被消费的时间点
        invitationCode.setUsedAt(LocalDateTime.now());
        // 若达到最大可用次数，将邀请码状态置为 USED，后续不可再次使用
        if (invitationCode.getUsedCount() >= invitationCode.getMaxUses()) {
            invitationCode.setStatus(SocConstants.Invitation.STATUS_USED);
        }
        // 持久化邀请码的使用人、使用时间、次数及状态变更
        invitationCodeMapper.updateUsage(invitationCode);

        // 邀请码若绑定了项目，则自动把新用户加入该项目成员
        if (invitationCode.getProjectId() != null) {
            // 查询该用户是否已是项目成员，避免重复插入
            ProjectMember existed = projectMemberMapper.selectByProjectIdAndUserId(invitationCode.getProjectId(), userAccount.getUserId());
            if (existed == null) {
                ProjectMember member = new ProjectMember();
                // 关联邀请码指定的目标项目
                member.setProjectId(invitationCode.getProjectId());
                // 关联刚注册成功的用户
                member.setUserId(userAccount.getUserId());
                // 成员角色取自邀请码预设角色（如 GENERAL_USER），并做归一化
                member.setMemberRole(defaultRole(invitationCode.getMemberRole()));
                // 冗余展示名与邮箱，便于成员列表直接展示
                member.setDisplayName(userAccount.getDisplayName());
                member.setEmail(userAccount.getEmail());
                // 新成员默认未删除
                member.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
                // 创建/更新人记为邀请码创建者（发起邀请的管理员）
                member.setCreatedBy(invitationCode.getCreatedBy());
                member.setUpdatedBy(invitationCode.getCreatedBy());
                // 写入项目成员表，完成「凭邀请码注册即入项」
                projectMemberMapper.insert(member);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void revoke(Long invitationId) {
        InvitationCode invitationCode = invitationCodeMapper.selectById(invitationId);
        if (invitationCode == null) {
            throw new BizException(BizErrorCode.INVITATION_NOT_FOUND);
        }
        authorizationService.requireProjectManage(invitationCode.getProjectId());
        invitationCodeMapper.revoke(invitationId);
        operationLogService.record(SocConstants.OperationLog.Module.INVITATION_CODE,
            SocConstants.OperationLog.Action.REVOKE,
            SocConstants.OperationLog.EntityType.INVITATION_CODE,
            String.valueOf(invitationId),
            SocConstants.Invitation.LABEL_GENERIC_ZH,
            null,
            SocConstants.OperationLog.Detail.INVITATION_REVOKE_ZH);
    }

    private boolean isUsable(InvitationCode invitationCode) {
        if (!SocConstants.Invitation.STATUS_ACTIVE.equalsIgnoreCase(invitationCode.getStatus())) {
            return false;
        }
        if (invitationCode.getExpiresAt() != null && invitationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return false;
        }
        return invitationCode.getUsedCount() < invitationCode.getMaxUses();
    }

    private BizErrorCode unusableInvitationReason(InvitationCode invitationCode) {
        if (SocConstants.Invitation.STATUS_REVOKED.equalsIgnoreCase(invitationCode.getStatus())) {
            return BizErrorCode.INVITATION_REVOKED;
        }
        if (SocConstants.Invitation.STATUS_USED.equalsIgnoreCase(invitationCode.getStatus())) {
            return BizErrorCode.INVITATION_USED;
        }
        if (invitationCode.getExpiresAt() != null && invitationCode.getExpiresAt().isBefore(LocalDateTime.now())) {
            return BizErrorCode.INVITATION_EXPIRED;
        }
        return BizErrorCode.INVITATION_UNAVAILABLE;
    }

    private String buildProjectCode(Project project) {
        long serial = invitationCodeMapper.countByProjectId(project.getProjectId()) + 1;
        Company company = companyMapper.selectById(project.getCompanyId());
        String companyName = company == null ? "公司" : company.getCompanyName();
        return sanitize(companyName, 16)
            + "-"
            + sanitize(project.getProjectName() == null ? "项目" : project.getProjectName(), 16)
            + "-"
            + String.format("%04d", serial);
    }

    private String sanitize(String text, int maxLength) {
        String value = text.replaceAll("\\s+", "").replaceAll("[\\\\/:*?\"<>|]", "");
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private String defaultRole(String role) {
        return RoleCodes.normalizeProjectRole(role);
    }
}
