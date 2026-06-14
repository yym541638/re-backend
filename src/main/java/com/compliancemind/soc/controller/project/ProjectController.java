package com.compliancemind.soc.controller.project;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.common.api.PageResponse;
import com.compliancemind.soc.dto.project.ProjectCompanyUserItem;
import com.compliancemind.soc.dto.project.ProjectCreateRequest;
import com.compliancemind.soc.dto.project.ProjectCreateResponse;
import com.compliancemind.soc.dto.project.ProjectDetailResponse;
import com.compliancemind.soc.dto.project.ProjectListItem;
import com.compliancemind.soc.dto.project.ProjectMemberSaveRequest;
import com.compliancemind.soc.dto.project.ProjectQueryRequest;
import com.compliancemind.soc.dto.project.ProjectRoleSlotItem;
import com.compliancemind.soc.dto.project.ProjectUpdateRequest;
import com.compliancemind.soc.entity.project.ProjectMember;
import com.compliancemind.soc.service.project.ProjectService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 合规项目：列表、详情、CRUD、成员维护。
 *
 * <p>对应 PRD 2.5.1 项目信息编辑、2.5.12 Project Settings。</p>
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

    private final ProjectService projectService;

    public ProjectController(ProjectService projectService) {
        this.projectService = projectService;
    }

    /**
     * 新建项目（JSON）。
     *
     * <p>POST /project/create，需 JWT；仅公司管理员可创建。
     * 请求体含项目基本信息及 {@code members} 项目维度角色分配（六个固定角色可部分填写）。</p>
     */
    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ApiResponse<ProjectCreateResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
        return ApiResponse.success(projectService.create(request));
    }

    /**
     * 项目列表（分页）。
     *
     * <p>GET /project/list，返回 UI 表格列：project_id、project_name、project_info、
     * start_date、end_date、last_modified_date。</p>
     */
    @GetMapping("/list")
    public ApiResponse<PageResponse<ProjectListItem>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                                           @RequestParam(value = "status", required = false) String status,
                                                           @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                                           @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        ProjectQueryRequest request = new ProjectQueryRequest();
        request.setKeyword(keyword);
        request.setStatus(status);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        return ApiResponse.page(projectService.list(request));
    }

    /**
     * Project User management 固定角色列表。
     *
     * <p>GET /project/role-slots，供创建/编辑表单渲染六个角色行。</p>
     */
    @GetMapping("/role-slots")
    public ApiResponse<List<ProjectRoleSlotItem>> roleSlots() {
        return ApiResponse.success(projectService.listRoleSlots());
    }

    /**
     * 按公司名称查询本公司用户列表。
     *
     * <p>GET /project/company/users?companyName=xxx&amp;keyword=，需 JWT；仅公司管理员可调用。
     * {@code companyName} 须与当前登录用户所属公司一致，用于创建项目时成员选择弹窗。</p>
     */
    @GetMapping("/company/users")
    public ApiResponse<List<ProjectCompanyUserItem>> listCompanyUsers(
            @RequestParam("companyName") String companyName,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.success(projectService.listCompanyUsers(companyName, keyword));
    }

    /**
     * 项目详情（含 Project specification 表单字段与 roleSlots）。
     */
    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> detail(@PathVariable("projectId") Long projectId) {
        return ApiResponse.success(projectService.detail(projectId));
    }

    /**
     * 编辑项目（Project specification 保存）。
     *
     * <p>可更新 projectName、projectInfo、startDate、endDate；传入 members 时全量覆盖项目成员。</p>
     */
    @PutMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> update(@PathVariable("projectId") Long projectId,
                                                     @Valid @RequestBody ProjectUpdateRequest request) {
        return ApiResponse.success(projectService.update(projectId, request));
    }

    @DeleteMapping("/{projectId}")
    public ApiResponse<Void> delete(@PathVariable("projectId") Long projectId) {
        projectService.delete(projectId);
        return ApiResponse.success();
    }

    /**
     * 保存项目成员（PRD 2.5.12）。
     *
     * <p>PUT /project/{projectId}/members；项目创建后也可通过此接口调整项目维度角色。</p>
     */
    @PutMapping("/{projectId}/members")
    public ApiResponse<List<ProjectMember>> saveMembers(@PathVariable("projectId") Long projectId,
                                                        @Valid @RequestBody ProjectMemberSaveRequest request) {
        return ApiResponse.success(projectService.saveMembers(projectId, request));
    }
}
