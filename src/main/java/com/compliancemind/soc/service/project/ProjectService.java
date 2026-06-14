package com.compliancemind.soc.service.project;

import com.compliancemind.soc.common.api.PageResponse;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.dto.project.ProjectCompanyUserItem;
import com.compliancemind.soc.dto.project.ProjectCreateRequest;
import com.compliancemind.soc.dto.project.ProjectCreateResponse;
import com.compliancemind.soc.dto.project.ProjectDetailResponse;
import com.compliancemind.soc.dto.project.ProjectListItem;
import com.compliancemind.soc.dto.project.ProjectMemberSaveRequest;
import com.compliancemind.soc.dto.project.ProjectQueryRequest;
import com.compliancemind.soc.dto.project.ProjectRoleSlotItem;
import com.compliancemind.soc.dto.project.ProjectUpdateRequest;
import com.compliancemind.soc.entity.auth.Company;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.project.ProjectMember;
import com.compliancemind.soc.mapper.auth.CompanyMapper;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.mapper.project.ProjectMemberMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.security.RoleCodes;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 合规项目业务：列表查询、详情组装、项目 CRUD 及成员全量保存。
 *
 * <p>公司级权限（sys_user.role_code）决定能否创建项目；
 * 项目级权限（soc_project_member.member_role）决定用户在该项目内的访问范围，实现子公司/项目隔离。</p>
 */
@Service
public class ProjectService {

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern(SocConstants.Format.COMPACT_TIMESTAMP);

    private static final List<Map.Entry<String, String>> PROJECT_ROLE_SLOTS = List.of(
        Map.entry(RoleCodes.COMPANY_ADMIN, "Administrator"),
        Map.entry(RoleCodes.PROJECT_OWNER, "Project Owner"),
        Map.entry(RoleCodes.DOCUMENT_OWNER, "Document Owner"),
        Map.entry(RoleCodes.GENERAL_USER, "General User"),
        Map.entry(RoleCodes.MANAGER, "1st tier Manager User"),
        Map.entry(RoleCodes.MANAGER_2, "2nd tier Manager User 1")
    );

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final UserAccountMapper userAccountMapper;
    private final CompanyMapper companyMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;

    public ProjectService(ProjectMapper projectMapper,
                          ProjectMemberMapper projectMemberMapper,
                          UserAccountMapper userAccountMapper,
                          CompanyMapper companyMapper,
                          AuthorizationService authorizationService,
                          CurrentUserAccessor currentUserAccessor,
                          OperationLogService operationLogService) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.userAccountMapper = userAccountMapper;
        this.companyMapper = companyMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
    }

    public PageResponse<ProjectListItem> list(ProjectQueryRequest request) {
        applyPagination(request);
        UserAccount currentUser = authorizationService.currentUser();
        if (authorizationService.canAccessAllProjects()) {
            long total = projectMapper.countAll(currentUser.getCompanyId(), request);
            List<Project> list = projectMapper.listAll(currentUser.getCompanyId(), request);
            return PageResponse.of(total, request.getPageNum(), request.getPageSize(), toListItems(list));
        }
        long total = projectMapper.countAllByMember(
            currentUser.getCompanyId(), currentUser.getUserId(), request);
        List<Project> list = projectMapper.listAllByMember(
            currentUser.getCompanyId(), currentUser.getUserId(), request);
        return PageResponse.of(total, request.getPageNum(), request.getPageSize(), toListItems(list));
    }

    private void applyPagination(ProjectQueryRequest request) {
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        request.setOffset((long) (pageNum - 1) * pageSize);
    }

    /**
     * 按公司名称查询本公司用户，供创建/编辑项目时选择成员。
     *
     * <p>{@code companyName} 必须与当前登录用户所属公司一致，防止跨公司越权查询。</p>
     */
    public List<ProjectCompanyUserItem> listCompanyUsers(String companyName, String keyword) {
        authorizationService.requireCompanyProjectManagement();
        UserAccount currentUser = authorizationService.currentUser();
        Company company = companyMapper.selectById(currentUser.getCompanyId());
        if (company == null || companyName == null || companyName.isBlank()
                || !company.getCompanyName().equalsIgnoreCase(companyName.trim())) {
            throw new BizException(BizErrorCode.COMPANY_NOT_FOUND);
        }
        return userAccountMapper.listUsers(currentUser.getCompanyId(), keyword).stream()
                .map(this::toCompanyUserItem)
                .toList();
    }

    public List<ProjectRoleSlotItem> listRoleSlots() {
        return PROJECT_ROLE_SLOTS.stream().map(entry -> {
            ProjectRoleSlotItem slot = new ProjectRoleSlotItem();
            slot.setRoleCode(entry.getKey());
            slot.setRoleName(entry.getValue());
            return slot;
        }).toList();
    }

    public ProjectDetailResponse detail(Long projectId) {
        Project project = authorizationService.requireProjectRead(projectId);
        List<ProjectMember> members = projectMemberMapper.listByProjectId(projectId);
        ProjectDetailResponse response = new ProjectDetailResponse();
        response.setProject(project);
        response.setMembers(members);
        response.setRoleSlots(buildRoleSlots(members));
        return response;
    }

    /**
     * 创建项目：写入项目基本信息、分配项目维度成员角色。
     */
    @Transactional(rollbackFor = Exception.class)
    public ProjectCreateResponse create(ProjectCreateRequest request) {
        authorizationService.requireCompanyProjectManagement();
        UserAccount currentUser = authorizationService.currentUser();

        Project project = new Project();
        project.setCompanyId(currentUser.getCompanyId());
        project.setProjectCode(SocConstants.Project.CODE_PREFIX + CODE_FORMATTER.format(LocalDateTime.now()));
        project.setProjectName(request.getProjectName().trim());
        project.setProjectInfo(normalizeProjectInfo(request.getProjectInfo()));
        project.setComplianceType(SocConstants.Ai.DEFAULT_COMPLIANCE_FRAMEWORK);
        project.setAuditType(SocConstants.AuditType.DISPLAY_TYPE1);
        project.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
        project.setGapCount(0);
        project.setStatus(SocConstants.Project.STATUS_ACTIVE);
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        project.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        project.setCreatedBy(currentUser.getUserId());
        project.setUpdatedBy(currentUser.getUserId());
        projectMapper.insert(project);

        List<ProjectMemberSaveRequest.MemberItem> memberItems =
            ensureCreatorInMembers(request.getMembers(), currentUser);
        List<ProjectMember> savedMembers = persistMembers(
            project.getProjectId(),
            currentUser.getCompanyId(),
            memberItems,
            currentUser.getUserId()
        );

        operationLogService.record(SocConstants.OperationLog.Module.PROJECT,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(project.getProjectId()),
            project.getProjectName(),
            project.getProjectId(),
            SocConstants.OperationLog.Detail.PROJECT_CREATE_ZH);

        ProjectCreateResponse response = new ProjectCreateResponse();
        response.setProject(project);
        response.setMembers(savedMembers);
        response.setRoleSlots(buildRoleSlots(savedMembers));
        response.setAttachments(List.of());
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public ProjectDetailResponse update(Long projectId, ProjectUpdateRequest request) {
        Project project = authorizationService.requireProjectManage(projectId);
        project.setProjectName(request.getProjectName().trim());
        if (request.getProjectInfo() != null) {
            project.setProjectInfo(normalizeProjectInfo(request.getProjectInfo()));
        }
        if (request.getStartDate() != null) {
            project.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            project.setEndDate(request.getEndDate());
        }
        project.setUpdatedBy(currentUserAccessor.requireUserId());
        projectMapper.update(project);

        if (request.getMembers() != null) {
            replaceMembers(projectId, project.getCompanyId(), request.getMembers());
        }

        operationLogService.record(SocConstants.OperationLog.Module.PROJECT,
            SocConstants.OperationLog.Action.UPDATE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(project.getProjectId()),
            project.getProjectName(),
            project.getProjectId(),
            SocConstants.OperationLog.Detail.PROJECT_UPDATE_ZH);
        return detail(projectId);
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
        Project project = authorizationService.requireProjectRead(projectId);
        List<ProjectMember> savedMembers = replaceMembers(projectId, project.getCompanyId(), request.getMembers());
        operationLogService.record(SocConstants.OperationLog.Module.PROJECT,
            SocConstants.OperationLog.Action.UPDATE_MEMBERS,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(projectId),
            project.getProjectName(),
            projectId,
            SocConstants.OperationLog.Detail.PROJECT_UPDATE_MEMBERS_ZH);
        return savedMembers;
    }

    @Transactional(rollbackFor = Exception.class)
    public void markProjectEnded(Long projectId) {
        authorizationService.requireProjectRead(projectId);
        projectMapper.updateStatusAndEndDate(projectId,
            SocConstants.Project.STATUS_END,
            LocalDateTime.now(),
            currentUserAccessor.requireUserId(),
            SocConstants.Project.STATUS_ACTIVE);
    }

    private List<ProjectMember> replaceMembers(Long projectId,
                                               Integer companyId,
                                               List<ProjectMemberSaveRequest.MemberItem> members) {
        Integer operatorId = currentUserAccessor.requireUserId();
        projectMemberMapper.softDeleteByProjectId(projectId, operatorId);
        return persistMembers(projectId, companyId, members, operatorId);
    }

    private List<ProjectMemberSaveRequest.MemberItem> ensureCreatorInMembers(
        List<ProjectMemberSaveRequest.MemberItem> members,
        UserAccount creator) {
        if (members == null || members.isEmpty()) {
            ProjectMemberSaveRequest.MemberItem creatorItem = new ProjectMemberSaveRequest.MemberItem();
            creatorItem.setUserId(creator.getUserId());
            creatorItem.setMemberRole(RoleCodes.COMPANY_ADMIN);
            creatorItem.setDisplayName(creator.getDisplayName());
            creatorItem.setEmail(creator.getEmail());
            return List.of(creatorItem);
        }
        boolean creatorIncluded = members.stream()
            .anyMatch(item -> creator.getUserId().equals(item.getUserId()));
        if (creatorIncluded) {
            return members;
        }
        ProjectMemberSaveRequest.MemberItem creatorItem = new ProjectMemberSaveRequest.MemberItem();
        creatorItem.setUserId(creator.getUserId());
        creatorItem.setMemberRole(RoleCodes.COMPANY_ADMIN);
        creatorItem.setDisplayName(creator.getDisplayName());
        creatorItem.setEmail(creator.getEmail());
        List<ProjectMemberSaveRequest.MemberItem> merged = new ArrayList<>(members);
        merged.add(creatorItem);
        return merged;
    }

    private List<ProjectMember> persistMembers(Long projectId,
                                               Integer companyId,
                                               List<ProjectMemberSaveRequest.MemberItem> members,
                                               Integer operatorId) {
        if (members == null || members.isEmpty()) {
            throw new BizException(BizErrorCode.PROJECT_MEMBERS_REQUIRED);
        }

        Set<Integer> assignedUserIds = new HashSet<>();
        Set<String> assignedRoles = new HashSet<>();
        boolean hasProjectManager = false;
        List<ProjectMember> savedMembers = new ArrayList<>();

        for (ProjectMemberSaveRequest.MemberItem item : members) {
            if (item.getUserId() == null) {
                throw new BizException(BizErrorCode.PROJECT_MEMBER_USER_NOT_IN_COMPANY);
            }
            if (!assignedUserIds.add(item.getUserId())) {
                throw new BizException(BizErrorCode.PROJECT_MEMBER_DUPLICATE_USER);
            }

            UserAccount user = userAccountMapper.selectByIdAndCompanyId(item.getUserId(), companyId);
            if (user == null) {
                throw new BizException(BizErrorCode.PROJECT_MEMBER_USER_NOT_IN_COMPANY);
            }

            String memberRole = RoleCodes.normalizeProjectRole(item.getMemberRole());
            if (!RoleCodes.isProjectRole(memberRole)) {
                throw new BizException(BizErrorCode.PROJECT_MEMBER_ROLE_UNSUPPORTED);
            }
            if (!assignedRoles.add(memberRole)) {
                throw new BizException(BizErrorCode.PROJECT_MEMBER_DUPLICATE_USER);
            }
            if (RoleCodes.canManageProject(memberRole)) {
                hasProjectManager = true;
            }

            ProjectMember member = new ProjectMember();
            member.setProjectId(projectId);
            member.setUserId(user.getUserId());
            member.setMemberRole(memberRole);
            member.setDisplayName(firstNonBlank(item.getDisplayName(), user.getDisplayName()));
            member.setEmail(firstNonBlank(item.getEmail(), user.getEmail()));
            member.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
            member.setCreatedBy(operatorId);
            member.setUpdatedBy(operatorId);
            projectMemberMapper.insert(member);
            savedMembers.add(member);
        }

        if (!hasProjectManager) {
            throw new BizException(BizErrorCode.PROJECT_MEMBER_MANAGER_REQUIRED);
        }
        return savedMembers;
    }

    private List<ProjectRoleSlotItem> buildRoleSlots(List<ProjectMember> members) {
        Map<String, ProjectMember> memberByRole = members == null ? Map.of() : members.stream()
            .collect(Collectors.toMap(
                member -> RoleCodes.normalizeProjectRole(member.getMemberRole()),
                member -> member,
                (left, right) -> left
            ));
        List<ProjectRoleSlotItem> slots = new ArrayList<>();
        for (Map.Entry<String, String> entry : PROJECT_ROLE_SLOTS) {
            ProjectRoleSlotItem slot = new ProjectRoleSlotItem();
            slot.setRoleCode(entry.getKey());
            slot.setRoleName(entry.getValue());
            ProjectMember member = memberByRole.get(entry.getKey());
            if (member != null) {
                slot.setUserId(member.getUserId());
                slot.setDisplayName(member.getDisplayName());
                slot.setEmail(member.getEmail());
            }
            slots.add(slot);
        }
        return slots;
    }

    private List<ProjectListItem> toListItems(List<Project> projects) {
        if (projects == null || projects.isEmpty()) {
            return List.of();
        }
        return projects.stream().map(project -> {
            ProjectListItem item = new ProjectListItem();
            item.setProjectId(project.getProjectId());
            item.setProjectName(project.getProjectName());
            item.setProjectInfo(project.getProjectInfo());
            item.setStartDate(project.getStartDate());
            item.setEndDate(project.getEndDate());
            item.setLastModifiedDate(project.getUpdatedAt());
            return item;
        }).toList();
    }

    private String normalizeProjectInfo(String projectInfo) {
        if (projectInfo == null) {
            return null;
        }
        String trimmed = projectInfo.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback;
    }

    private ProjectCompanyUserItem toCompanyUserItem(UserAccount user) {
        ProjectCompanyUserItem item = new ProjectCompanyUserItem();
        item.setUserId(user.getUserId());
        item.setDisplayName(user.getDisplayName());
        item.setEmail(user.getEmail());
        item.setPhone(user.getPhone());
        item.setPermissionCode(RoleCodes.normalizeCompanyRole(user.getRoleCode()));
        return item;
    }
}
