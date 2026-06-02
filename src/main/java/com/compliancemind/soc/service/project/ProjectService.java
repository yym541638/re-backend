package com.compliancemind.soc.service.project;

import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.common.storage.LocalStorageService;
import com.compliancemind.soc.dto.project.ProjectCompanyUserItem;
import com.compliancemind.soc.dto.project.ProjectCreateRequest;
import com.compliancemind.soc.dto.project.ProjectCreateResponse;
import com.compliancemind.soc.dto.project.ProjectDetailResponse;
import com.compliancemind.soc.dto.project.ProjectMemberSaveRequest;
import com.compliancemind.soc.dto.project.ProjectQueryRequest;
import com.compliancemind.soc.dto.project.ProjectUpdateRequest;
import com.compliancemind.soc.entity.auth.Company;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.project.ProjectAttachment;
import com.compliancemind.soc.entity.project.ProjectMember;
import com.compliancemind.soc.mapper.auth.CompanyMapper;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.mapper.project.ProjectAttachmentMapper;
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.mapper.project.ProjectMemberMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.security.RoleCodes;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 合规项目业务：列表查询、详情组装、项目 CRUD 及成员全量保存。
 *
 * <p>公司级权限（sys_user.role_code）决定能否创建项目；
 * 项目级权限（soc_project_member.member_role）决定用户在该项目内的访问范围，实现子公司/项目隔离。</p>
 */
@Service
public class ProjectService {

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern(SocConstants.Format.COMPACT_TIMESTAMP);

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;
    private final ProjectAttachmentMapper projectAttachmentMapper;
    private final UserAccountMapper userAccountMapper;
    private final CompanyMapper companyMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;
    private final LocalStorageService localStorageService;

    public ProjectService(ProjectMapper projectMapper,
                          ProjectMemberMapper projectMemberMapper,
                          ProjectAttachmentMapper projectAttachmentMapper,
                          UserAccountMapper userAccountMapper,
                          CompanyMapper companyMapper,
                          AuthorizationService authorizationService,
                          CurrentUserAccessor currentUserAccessor,
                          OperationLogService operationLogService,
                          LocalStorageService localStorageService) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
        this.projectAttachmentMapper = projectAttachmentMapper;
        this.userAccountMapper = userAccountMapper;
        this.companyMapper = companyMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
        this.localStorageService = localStorageService;
    }

    public List<Project> list(ProjectQueryRequest request) {
        UserAccount currentUser = authorizationService.currentUser();
        if (authorizationService.canAccessAllProjects()) {
            return projectMapper.listAll(currentUser.getCompanyId(), request);
        }
        return projectMapper.listAllByMember(currentUser.getCompanyId(), currentUser.getUserId(), request);
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

    public ProjectDetailResponse detail(Long projectId) {
        Project project = authorizationService.requireProjectRead(projectId);
        ProjectDetailResponse response = new ProjectDetailResponse();
        response.setProject(project);
        response.setMembers(projectMemberMapper.listByProjectId(projectId));
        return response;
    }

    /**
     * 创建项目：写入项目基本信息、分配项目维度成员角色、可选保存上传文件。
     */
    @Transactional(rollbackFor = Exception.class)
    public ProjectCreateResponse create(ProjectCreateRequest request, List<MultipartFile> files) {
        // 校验当前用户是否具备公司级项目管理权限（仅公司管理员可创建项目）
        authorizationService.requireCompanyProjectManagement();
        // 获取当前登录用户，用于填充 companyId 及审计字段
        UserAccount currentUser = authorizationService.currentUser();

        Project project = new Project();
        // 项目归属当前用户所在公司
        project.setCompanyId(currentUser.getCompanyId());
        // 自动生成项目编号：固定前缀 + 当前时间戳
        project.setProjectCode(SocConstants.Project.CODE_PREFIX + CODE_FORMATTER.format(LocalDateTime.now()));
        project.setProjectName(request.getProjectName().trim());
        // 合规类型未传时默认 SOC2
        project.setComplianceType(resolveComplianceType(request.getComplianceType()));
        // 审计类型当前固定 Type1
        project.setAuditType(SocConstants.AuditType.DISPLAY_TYPE1);
        project.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
        // gap 数量在 Gap Analysis 生成后更新
        project.setGapCount(0);
        // 新建项目默认 Active，End 由 Passing Scores 完成后系统更新
        project.setStatus(SocConstants.Project.STATUS_ACTIVE);
        project.setStartDate(request.getStartDate());
        // 结束日期在 Passing Scores 打分完成后由系统写入
        project.setEndDate(null);
        project.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        project.setCreatedBy(currentUser.getUserId());
        project.setUpdatedBy(currentUser.getUserId());
        // 持久化项目记录（insert 后 projectId 由数据库回填）
        projectMapper.insert(project);

        // 若创建者未在 members 中，自动补为项目 Administrator
        List<ProjectMemberSaveRequest.MemberItem> memberItems = ensureCreatorInMembers(request.getMembers(), currentUser);
        // 校验并写入项目维度成员角色（须为本公司用户、不可重复）
        List<ProjectMember> savedMembers = persistMembers(
            project.getProjectId(),
            currentUser.getCompanyId(),
            memberItems,
            currentUser.getUserId()
        );

        // 保存创建页上传的附件（可选，pdf/word/excel）
        List<ProjectAttachment> attachments = storeAttachments(project.getProjectId(), files, currentUser.getUserId());

        // 记录项目创建操作日志
        operationLogService.record(SocConstants.OperationLog.Module.PROJECT,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(project.getProjectId()),
            project.getProjectName(),
            project.getProjectId(),
            SocConstants.OperationLog.Detail.PROJECT_CREATE_ZH);

        // 组装创建结果：项目 + 成员 + 附件
        ProjectCreateResponse response = new ProjectCreateResponse();
        response.setProject(project);
        response.setMembers(savedMembers);
        response.setAttachments(attachments);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public Project update(Long projectId, ProjectUpdateRequest request) {
        Project project = authorizationService.requireProjectManage(projectId);
        project.setProjectName(request.getProjectName().trim());
        project.setComplianceType(request.getComplianceType().trim());
        project.setAuditType(request.getAuditType().trim());
        project.setStartDate(request.getStartDate());
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
        Project project = authorizationService.requireProjectRead(projectId);
        Integer operatorId = currentUserAccessor.requireUserId();
        projectMemberMapper.softDeleteByProjectId(projectId, operatorId);
        List<ProjectMember> savedMembers = persistMembers(
            projectId,
            project.getCompanyId(),
            request.getMembers(),
            operatorId
        );
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
            LocalDate.now(),
            currentUserAccessor.requireUserId(),
            SocConstants.Project.STATUS_ACTIVE);
    }

    private List<ProjectMemberSaveRequest.MemberItem> ensureCreatorInMembers(
        List<ProjectMemberSaveRequest.MemberItem> members,
        UserAccount creator) {
        if (members == null || members.isEmpty()) {
            throw new BizException(BizErrorCode.PROJECT_MEMBERS_REQUIRED);
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

    private List<ProjectAttachment> storeAttachments(Long projectId, List<MultipartFile> files, Integer operatorId) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }
        List<ProjectAttachment> attachments = new ArrayList<>();
        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                continue;
            }
            LocalStorageService.StoredFile storedFile = localStorageService.storeProjectAttachment(projectId, file);
            ProjectAttachment attachment = new ProjectAttachment();
            attachment.setProjectId(projectId);
            attachment.setFileName(storedFile.originalName());
            attachment.setFilePath(storedFile.filePath());
            attachment.setFileSize(storedFile.fileSize());
            attachment.setCreatedBy(operatorId);
            projectAttachmentMapper.insert(attachment);
            attachments.add(attachment);
        }
        return attachments;
    }

    private String resolveComplianceType(String complianceType) {
        if (complianceType == null || complianceType.isBlank()) {
            return SocConstants.Ai.DEFAULT_COMPLIANCE_FRAMEWORK;
        }
        return complianceType.trim();
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
        item.setUserType(user.getUserType());
        return item;
    }
}
