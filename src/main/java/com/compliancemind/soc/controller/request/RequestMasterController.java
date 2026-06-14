package com.compliancemind.soc.controller.request;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.dto.request.RequestMasterCreateRequest;
import com.compliancemind.soc.dto.request.RequestMasterDetailResponse;
import com.compliancemind.soc.dto.request.RequestMasterListItem;
import com.compliancemind.soc.dto.request.RequestMasterStatusOption;
import com.compliancemind.soc.dto.request.RequestMasterUpdateRequest;
import com.compliancemind.soc.service.request.RequestMasterService;
import jakarta.validation.Valid;
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
 * Request Master（PRD 2.5.2）：进入项目后的 Request Master 列表与规格表单。
 */
@RestController
@RequestMapping("/request-master")
public class RequestMasterController {

    private final RequestMasterService requestMasterService;

    public RequestMasterController(RequestMasterService requestMasterService) {
        this.requestMasterService = requestMasterService;
    }

    /**
     * Request Master 列表。
     *
     * <p>GET /request-master/list?projectId=，需 JWT；从项目列表进入后按项目筛选。</p>
     */
    @GetMapping("/list")
    public ApiResponse<List<RequestMasterListItem>> list(
            @RequestParam(value = "projectId", required = false) Long projectId,
            @RequestParam(value = "project_id", required = false) Long projectIdLegacy) {
        Long resolvedProjectId = projectId != null ? projectId : projectIdLegacy;
        return ApiResponse.success(requestMasterService.list(resolvedProjectId));
    }

    /**
     * RequestMasterStatus 下拉选项。
     */
    @GetMapping("/status-options")
    public ApiResponse<List<RequestMasterStatusOption>> statusOptions() {
        return ApiResponse.success(requestMasterService.listStatusOptions());
    }

    /**
     * Request Master 详情（Request master specification 表单回显）。
     */
    @GetMapping("/{requestMasterId}")
    public ApiResponse<RequestMasterDetailResponse> detail(
            @PathVariable("requestMasterId") Long requestMasterId) {
        return ApiResponse.success(requestMasterService.detail(requestMasterId));
    }

    /**
     * 新建 Request Master。
     */
    @PostMapping
    public ApiResponse<RequestMasterDetailResponse> create(
            @Valid @RequestBody RequestMasterCreateRequest request) {
        return ApiResponse.success(requestMasterService.create(request));
    }

    /**
     * 新建 Request Master（兼容路径）。
     */
    @PostMapping("/create")
    public ApiResponse<RequestMasterDetailResponse> legacyCreate(
            @Valid @RequestBody RequestMasterCreateRequest request) {
        return ApiResponse.success(requestMasterService.create(request));
    }

    /**
     * 编辑 Request Master。
     *
     * <p>COMPLETED / CANCELLED 状态不可修改。</p>
     */
    @PutMapping("/{requestMasterId}")
    public ApiResponse<RequestMasterDetailResponse> update(
            @PathVariable("requestMasterId") Long requestMasterId,
            @Valid @RequestBody RequestMasterUpdateRequest request) {
        return ApiResponse.success(requestMasterService.update(requestMasterId, request));
    }

    /**
     * 删除 Request Master（软删除）。
     */
    @DeleteMapping("/{requestMasterId}")
    public ApiResponse<Void> delete(@PathVariable("requestMasterId") Long requestMasterId) {
        requestMasterService.delete(requestMasterId);
        return ApiResponse.success();
    }
}
