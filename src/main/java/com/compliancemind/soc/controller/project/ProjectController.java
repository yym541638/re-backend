package com.compliancemind.soc.controller.project;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.dto.project.ProjectCompanyUserItem;
import com.compliancemind.soc.dto.project.ProjectCreateRequest;
import com.compliancemind.soc.dto.project.ProjectCreateResponse;
import com.compliancemind.soc.dto.project.ProjectDetailResponse;
import com.compliancemind.soc.dto.project.ProjectMemberSaveRequest;
import com.compliancemind.soc.dto.project.ProjectQueryRequest;
import com.compliancemind.soc.dto.project.ProjectUpdateRequest;
import com.compliancemind.soc.entity.project.Project;
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
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
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
     * 请求体含项目基本信息及 {@code members} 项目维度角色分配（从本公司用户选取）。</p>
     */
//    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
//    public ApiResponse<ProjectCreateResponse> create(@Valid @RequestBody ProjectCreateRequest request) {
//        return ApiResponse.success(projectService.create(request, Collections.emptyList()));
//    }

    /**
     * 新建项目（含文件上传）。
     *
     * <p>POST /project/create，Content-Type: multipart/form-data。
     * part {@code project} 为 JSON（同 JSON 接口）；part {@code file} 可选，支持 pdf/word/excel。</p>
     */
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<ProjectCreateResponse> createWithFiles(@RequestPart("project") @Valid ProjectCreateRequest request,
                                                              @RequestPart(value = "file", required = false) MultipartFile file) {
        List<MultipartFile> files = file == null ? Collections.emptyList() : List.of(file);
        return ApiResponse.success(projectService.create(request, files));
    }

    @GetMapping("/list")
    public ApiResponse<List<Project>> list(@RequestParam(value = "keyword", required = false) String keyword,
                                           @RequestParam(value = "status", required = false) String status) {
        ProjectQueryRequest request = new ProjectQueryRequest();
        request.setKeyword(keyword);
        request.setStatus(status);
        return ApiResponse.success(projectService.list(request));
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

    @GetMapping("/{projectId}")
    public ApiResponse<ProjectDetailResponse> detail(@PathVariable("projectId") Long projectId) {
        return ApiResponse.success(projectService.detail(projectId));
    }

    @PutMapping("/{projectId}")
    public ApiResponse<Project> update(@PathVariable("projectId") Long projectId,
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
