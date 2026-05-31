package com.compliancemind.soc.controller.project;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.dto.project.ProjectCreateRequest;

import com.compliancemind.soc.dto.project.ProjectDetailResponse;

import com.compliancemind.soc.dto.project.ProjectMemberSaveRequest;

import com.compliancemind.soc.dto.project.ProjectQueryRequest;

import com.compliancemind.soc.dto.project.ProjectUpdateRequest;

import com.compliancemind.soc.entity.project.Project;

import com.compliancemind.soc.entity.project.ProjectMember;

import com.compliancemind.soc.service.project.ProjectService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;



import java.util.List;



/**

 * 合规项目：列表、详情（新旧路径）、CRUD、成员维护。

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

     * 项目列表（PRD 2.5.1）。

     * <p>GET /project/list，需 JWT；支持 keyword、status 筛选，返回本公司项目。</p>

     *

     * @param keyword 关键字搜索（可选）

     * @param status  项目状态筛选（可选）

     * @return 项目列表

     */

    @GetMapping("/list")

    public ApiResponse<List<Project>> list(@RequestParam(value = "keyword", required = false) String keyword,

                                                   @RequestParam(value = "status", required = false) String status) {

        ProjectQueryRequest request = new ProjectQueryRequest();

        request.setKeyword(keyword);

        request.setStatus(status);

        return ApiResponse.success(projectService.list(request));

    }



    /**

     * 项目详情（PRD 2.5.1 / 2.5.12）。

     * <p>GET /project/{projectId}，需 JWT；含项目成员信息。</p>

     *

     * @param projectId 项目 ID

     * @return 项目详情及成员列表

     */

    @GetMapping("/{projectId}")

    public ApiResponse<ProjectDetailResponse> detail(@PathVariable("projectId") Long projectId) {

        return ApiResponse.success(projectService.detail(projectId));

    }



    /**

     * 项目详情（旧版兼容路径）。

     * <p>GET /project/detail/{project_id}，行为与 {@link #detail(Long)} 一致。</p>

     *

     * @param projectId 项目 ID

     * @return 项目详情及成员列表

     */

    @GetMapping("/detail/{project_id}")

    public ApiResponse<ProjectDetailResponse> legacyDetail(@PathVariable("project_id") Long projectId) {

        return ApiResponse.success(projectService.detail(projectId));

    }



    /**

     * 新建项目（PRD 2.5.1）。

     * <p>POST /project，需 JWT；仅公司管理员可创建。</p>

     *

     * @param request 项目名称、合规类型、审计类型、状态及日期等

     * @return 新建的项目

     */

    @PostMapping

    public ApiResponse<Project> create(@Valid @RequestBody ProjectCreateRequest request) {

        return ApiResponse.success(projectService.create(request));

    }



    /**

     * 新建项目（旧版兼容路径）。

     * <p>POST /project/create，行为与 {@link #create(ProjectCreateRequest)} 一致。</p>

     *

     * @param request 项目创建字段

     * @return 新建的项目

     */

    @PostMapping("/create")

    public ApiResponse<Project> legacyCreate(@Valid @RequestBody ProjectCreateRequest request) {

        return ApiResponse.success(projectService.create(request));

    }



    /**

     * 编辑项目（PRD 2.5.1）。

     * <p>PUT /project/{projectId}，需 JWT；仅公司管理员可编辑。</p>

     *

     * @param projectId 项目 ID

     * @param request   项目更新字段

     * @return 更新后的项目

     */

    @PutMapping("/{projectId}")

    public ApiResponse<Project> update(@PathVariable("projectId") Long projectId,

                                       @Valid @RequestBody ProjectUpdateRequest request) {

        return ApiResponse.success(projectService.update(projectId, request));

    }



    /**

     * 删除项目（PRD 2.5.1）。

     * <p>DELETE /project/{projectId}，需 JWT；软删除，仅公司管理员可操作。</p>

     *

     * @param projectId 项目 ID

     * @return 操作成功空响应

     */

    @DeleteMapping("/{projectId}")

    public ApiResponse<Void> delete(@PathVariable("projectId") Long projectId) {

        projectService.delete(projectId);

        return ApiResponse.success();

    }



    /**

     * 保存项目成员（PRD 2.5.12）。

     * <p>PUT /project/{projectId}/members，需 JWT；仅公司管理员可维护成员及角色。</p>

     *

     * @param projectId 项目 ID

     * @param request   成员列表（userId、memberRole、displayName、email）

     * @return 保存后的成员列表

     */

    @PutMapping("/{projectId}/members")

    public ApiResponse<List<ProjectMember>> saveMembers(@PathVariable("projectId") Long projectId,

                                                        @Valid @RequestBody ProjectMemberSaveRequest request) {

        return ApiResponse.success(projectService.saveMembers(projectId, request));

    }

}


