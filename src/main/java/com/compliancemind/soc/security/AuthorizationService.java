package com.compliancemind.soc.security;

import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.project.ProjectMember;
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.mapper.project.ProjectMemberMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 公司级 / 项目级权限校验：管理员、成员角色、只读约束等。
 *
 * <p>可通过 {@code app.security.authorization-enabled} 临时关闭角色/成员校验；
 * 关闭时仍校验登录用户、项目存在且属于当前公司。
 * TODO: 业务开发完成后将 app.security.authorization-enabled 改回 true</p>
 */
@Component
public class AuthorizationService {

    private final UserAccountMapper userAccountMapper;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final CurrentUserAccessor currentUserAccessor;
    private final boolean authorizationEnabled;

    public AuthorizationService(UserAccountMapper userAccountMapper,
                                ProjectMapper projectMapper,
                                ProjectMemberMapper projectMemberMapper,
                                CurrentUserAccessor currentUserAccessor,
                                @Value("${app.security.authorization-enabled:true}") boolean authorizationEnabled) {
        this.userAccountMapper = userAccountMapper;
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.currentUserAccessor = currentUserAccessor;
        this.authorizationEnabled = authorizationEnabled;
    }

    public UserAccount currentUser() {
        UserAccount userAccount = userAccountMapper.selectById(currentUserAccessor.requireUserId());
        if (userAccount == null) {
            throw new BizException(BizErrorCode.AUTH_CURRENT_USER_NOT_FOUND);
        }
        userAccount.setRoleCode(RoleCodes.normalizeCompanyRole(userAccount.getRoleCode()));
        return userAccount;
    }

    public String currentRoleCode() {
        return currentUser().getRoleCode();
    }

    public Integer currentCompanyId() {
        return currentUser().getCompanyId();
    }

    public void requireCompanyManagement() {
        if (!authorizationEnabled) {
            return;
        }
        if (!RoleCodes.canManageCompany(currentRoleCode())) {
            throw new BizException(BizErrorCode.AUTH_COMPANY_ADMIN_REQUIRED);
        }
    }

    public void requireCompanyProjectManagement() {
        if (!authorizationEnabled) {
            return;
        }
        if (!RoleCodes.canAccessAllProjects(currentRoleCode())) {
            throw new BizException(BizErrorCode.AUTH_PROJECT_MANAGE_DENIED);
        }
    }

    /**
     * 项目成员目录：系统管理员或本公司任一项目管理员可访问。
     */
    public void requireProjectUserDirectoryAccess() {
        if (!authorizationEnabled) {
            return;
        }
        if (RoleCodes.canManageCompany(currentRoleCode())) {
            return;
        }
        if (canManageAnyProjectInCompany()) {
            return;
        }
        throw new BizException(BizErrorCode.AUTH_PROJECT_MANAGE_DENIED);
    }

    public boolean isSystemAdmin() {
        if (!authorizationEnabled) {
            return true;
        }
        return RoleCodes.canManageCompany(currentRoleCode());
    }

    public boolean canManageAnyProjectInCompany() {
        if (!authorizationEnabled) {
            return true;
        }
        if (RoleCodes.canManageCompany(currentRoleCode())) {
            return true;
        }
        Integer userId = currentUserAccessor.requireUserId();
        Integer companyId = currentCompanyId();
        return projectMemberMapper.listByUserIdAndCompanyId(userId, companyId).stream()
            .anyMatch(member -> RoleCodes.canManageProject(member.getMemberRole()));
    }

    public boolean canAccessAllProjects() {
        if (!authorizationEnabled) {
            return true;
        }
        return RoleCodes.canAccessAllProjects(currentRoleCode());
    }

    public Project requireProjectRead(Long projectId) {
        Project project = loadProject(projectId);
        if (!authorizationEnabled) {
            return project;
        }
        UserAccount currentUser = currentUser();
        if (RoleCodes.canAccessAllProjects(currentUser.getRoleCode())) {
            return project;
        }
        requireProjectMember(projectId, currentUser.getUserId());
        return project;
    }

    public Project requireProjectWrite(Long projectId) {
        if (!authorizationEnabled) {
            return loadProject(projectId);
        }
        Project project = requireProjectRead(projectId);
        if (RoleCodes.canAccessAllProjects(currentRoleCode())) {
            return project;
        }
        ProjectMember member = requireProjectMember(projectId, currentUserAccessor.requireUserId());
        if (!RoleCodes.canEditProjectContent(member.getMemberRole())) {
            throw new BizException(BizErrorCode.AUTH_PROJECT_READ_ONLY);
        }
        return project;
    }

    public Project requireProjectManage(Long projectId) {
        if (!authorizationEnabled) {
            return loadProject(projectId);
        }
        Project project = requireProjectRead(projectId);
        if (RoleCodes.canAccessAllProjects(currentRoleCode())) {
            return project;
        }
        ProjectMember member = requireProjectMember(projectId, currentUserAccessor.requireUserId());
        if (!RoleCodes.canManageProject(member.getMemberRole())) {
            throw new BizException(BizErrorCode.AUTH_PROJECT_MANAGE_DENIED);
        }
        return project;
    }

    private Project loadProject(Long projectId) {
        if (projectId == null) {
            throw new BizException(BizErrorCode.PROJECT_ID_REQUIRED);
        }
        Project project = projectMapper.selectById(projectId);
        if (project == null || !currentCompanyId().equals(project.getCompanyId())) {
            throw new BizException(BizErrorCode.PROJECT_NOT_FOUND);
        }
        return project;
    }

    private ProjectMember requireProjectMember(Long projectId, Integer userId) {
        ProjectMember member = projectMemberMapper.selectByProjectIdAndUserId(projectId, userId);
        if (member == null) {
            throw new BizException(BizErrorCode.AUTH_USER_NOT_IN_PROJECT);
        }
        member.setMemberRole(RoleCodes.normalizeProjectRole(member.getMemberRole()));
        return member;
    }
}
