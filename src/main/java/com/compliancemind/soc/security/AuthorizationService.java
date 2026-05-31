package com.compliancemind.soc.security;

import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.project.ProjectMember;
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.mapper.project.ProjectMemberMapper;
import org.springframework.stereotype.Component;

/**
 * 公司级 / 项目级权限校验：管理员、成员角色、只读约束等。
 */
@Component
public class AuthorizationService {

    private final UserAccountMapper userAccountMapper;
    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final CurrentUserAccessor currentUserAccessor;

    public AuthorizationService(UserAccountMapper userAccountMapper,
                                ProjectMapper projectMapper,
                                ProjectMemberMapper projectMemberMapper,
                                CurrentUserAccessor currentUserAccessor) {
        this.userAccountMapper = userAccountMapper;
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.currentUserAccessor = currentUserAccessor;
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
        if (!RoleCodes.canManageCompany(currentRoleCode())) {
            throw new BizException(BizErrorCode.AUTH_COMPANY_ADMIN_REQUIRED);
        }
    }

    public void requireCompanyProjectManagement() {
        if (!RoleCodes.canAccessAllProjects(currentRoleCode())) {
            throw new BizException(BizErrorCode.AUTH_PROJECT_MANAGE_DENIED);
        }
    }

    public boolean canAccessAllProjects() {
        return RoleCodes.canAccessAllProjects(currentRoleCode());
    }

    public Project requireProjectRead(Long projectId) {
        Project project = loadProject(projectId);
        UserAccount currentUser = currentUser();
        if (RoleCodes.canAccessAllProjects(currentUser.getRoleCode())) {
            return project;
        }
        requireProjectMember(projectId, currentUser.getUserId());
        return project;
    }

    public Project requireProjectWrite(Long projectId) {
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
