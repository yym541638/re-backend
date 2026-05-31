package com.compliancemind.soc.service.project;

import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.compliancemind.soc.dto.project.ProjectCreateRequest;
import com.compliancemind.soc.dto.project.ProjectDetailResponse;
import com.compliancemind.soc.dto.project.ProjectMemberSaveRequest;
import com.compliancemind.soc.dto.project.ProjectQueryRequest;
import com.compliancemind.soc.dto.project.ProjectUpdateRequest;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 项目及成员维护：创建项目、权限校验、成员角色保存。
 */
@Service
public class ProjectService {

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern(SocConstants.Format.COMPACT_TIMESTAMP);

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserAccountMapper userAccountMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;

    public ProjectService(ProjectMapper projectMapper,
                          ProjectMemberMapper projectMemberMapper,
                          UserAccountMapper userAccountMapper,
                          AuthorizationService authorizationService,
                          CurrentUserAccessor currentUserAccessor,
                          OperationLogService operationLogService) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.userAccountMapper = userAccountMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
    }

    public List<Project> list(ProjectQueryRequest request) {
        UserAccount currentUser = authorizationService.currentUser();
        if (authorizationService.canAccessAllProjects()) {
            return projectMapper.listAll(currentUser.getCompanyId(), request);
        }
        return projectMapper.listAllByMember(currentUser.getCompanyId(), currentUser.getUserId(), request);
    }

    public ProjectDetailResponse detail(Long projectId) {
        Project project = authorizationService.requireProjectRead(projectId);
        ProjectDetailResponse response = new ProjectDetailResponse();
        response.setProject(project);
        response.setMembers(projectMemberMapper.listByProjectId(projectId));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Project create(ProjectCreateRequest request) {
        authorizationService.requireCompanyProjectManagement();
        UserAccount currentUser = authorizationService.currentUser();
        Project project = new Project();
        project.setCompanyId(currentUser.getCompanyId());
        project.setProjectCode(SocConstants.Project.CODE_PREFIX + CODE_FORMATTER.format(LocalDateTime.now()));
        project.setProjectName(request.getProjectName().trim());
        project.setComplianceType(request.getComplianceType().trim());
        project.setAuditType(request.getAuditType().trim());
        project.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
        project.setGapCount(0);
        project.setStatus(request.getStatus().trim());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        project.setCreatedBy(currentUser.getUserId());
        project.setUpdatedBy(currentUser.getUserId());
        projectMapper.insert(project);
        operationLogService.record(SocConstants.OperationLog.Module.PROJECT,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(project.getProjectId()),
            project.getProjectName(),
            project.getProjectId(),
            SocConstants.OperationLog.Detail.PROJECT_CREATE_ZH);
        return project;
    }

    @Transactional(rollbackFor = Exception.class)
    public Project update(Long projectId, ProjectUpdateRequest request) {
        Project project = authorizationService.requireProjectManage(projectId);
        project.setProjectName(request.getProjectName().trim());
        project.setComplianceType(request.getComplianceType().trim());
        project.setAuditType(request.getAuditType().trim());
        project.setStatus(request.getStatus().trim());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setUpdatedBy(currentUserAccessor.requireUserId());
        projectMapper.update(project);
        operationLogService.record(SocConstants.OperationLog.Module.PROJECT,
            SocConstants.OperationLog.Action.UPDATE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(project.getProjectId()),
            project.getProjectName(),
            project.getProjectId(),
            SocConstants.OperationLog.Detail.PROJECT_UPDATE_ZH);
        return project;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long projectId) {
        authorizationService.requireProjectManage(projectId);
        Integer operatorId = currentUserAccessor.requireUserId();
        Project project = projectMapper.selectById(projectId);
        projectMapper.softDelete(projectId, operatorId);
        projectMemberMapper.softDeleteByProjectId(projectId, operatorId);
        operationLogService.record(SocConstants.OperationLog.Module.PROJECT,
            SocConstants.OperationLog.Action.DELETE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(projectId),
            project == null ? "" : project.getProjectName(),
            projectId,
            SocConstants.OperationLog.Detail.PROJECT_DELETE_ZH);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<ProjectMember> saveMembers(Long projectId, ProjectMemberSaveRequest request) {
        authorizationService.requireProjectManage(projectId);
        Integer operatorId = currentUserAccessor.requireUserId();
        projectMemberMapper.softDeleteByProjectId(projectId, operatorId);

        List<ProjectMember> savedMembers = new ArrayList<>();
        for (ProjectMemberSaveRequest.MemberItem item : request.getMembers()) {
            String memberRole = RoleCodes.normalizeProjectRole(item.getMemberRole());
            if (!RoleCodes.isProjectRole(memberRole)) {
                throw new BizException(BizErrorCode.PROJECT_MEMBER_ROLE_UNSUPPORTED);
            }
            ProjectMember member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(item.getUserId());
            member.setMemberRole(memberRole);
            member.setDisplayName(item.getDisplayName());
            member.setEmail(item.getEmail());
            member.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
            member.setCreatedBy(operatorId);
            member.setUpdatedBy(operatorId);
            projectMemberMapper.insert(member);
            savedMembers.add(member);
        }
        operationLogService.record(SocConstants.OperationLog.Module.PROJECT,
            SocConstants.OperationLog.Action.UPDATE_MEMBERS,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(projectId),
            authorizationService.requireProjectRead(projectId).getProjectName(),
            projectId,
            SocConstants.OperationLog.Detail.PROJECT_UPDATE_MEMBERS_ZH);
        return savedMembers;
    }

    private UserAccount currentUser() {
        Integer userId = currentUserAccessor.requireUserId();
        UserAccount userAccount = userAccountMapper.selectById(userId);
        if (userAccount == null) {
            throw new BizException(BizErrorCode.AUTH_CURRENT_USER_NOT_FOUND);
        }
        userAccount.setRoleCode(RoleCodes.normalizeCompanyRole(userAccount.getRoleCode()));
        return userAccount;
    }

}
