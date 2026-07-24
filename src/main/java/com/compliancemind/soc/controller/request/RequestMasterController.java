package com.compliancemind.soc.controller.request;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.common.api.PageResponse;
import com.compliancemind.soc.dto.request.RequestMasterCreateRequest;
import com.compliancemind.soc.dto.request.RequestMasterDetailResponse;
import com.compliancemind.soc.dto.request.RequestMasterListItem;
import com.compliancemind.soc.dto.request.RequestMasterStatusOption;
import com.compliancemind.soc.dto.request.RequestMasterTemplateFileItem;
import com.compliancemind.soc.dto.request.RequestMasterUpdateRequest;
import com.compliancemind.soc.dto.request.RequestMasterVersionDetailResponse;
import com.compliancemind.soc.dto.request.RequestMasterVersionListItem;
import com.compliancemind.soc.service.request.RequestMasterService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Request Master：Master 本身的列表、新建、编辑、删除、状态，以及挂在 Master 上的模板/版本。
 *
 * <p>某个 Master 下的 Individual 条目见 {@link RequestIndividualController}。</p>
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
     * <p>GET /request-master/list?projectId=</p>
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
     * 下载 Individual 批量导入 CSV 模板。
     */
    @GetMapping("/template-files/download-template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] content = requestMasterService.downloadTemplateFile();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"request_individual_template.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(content);
    }

    /**
     * Request Master 详情。
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

    /**
     * 模板文件列表（分页）。
     */
    @GetMapping("/{requestMasterId}/template-files")
    public ApiResponse<PageResponse<RequestMasterTemplateFileItem>> listTemplateFiles(
            @PathVariable("requestMasterId") Long requestMasterId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return ApiResponse.page(requestMasterService.listTemplateFiles(requestMasterId, pageNum, pageSize));
    }

    /**
     * 上传模板文件。
     */
    @PostMapping(value = "/{requestMasterId}/template-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<RequestMasterTemplateFileItem> uploadTemplateFile(
            @PathVariable("requestMasterId") Long requestMasterId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "relevantCriteria", required = false) String relevantCriteria,
            @RequestParam(value = "relevant_criteria", required = false) String relevantCriteriaLegacy) {
        String criteria = relevantCriteria != null ? relevantCriteria : relevantCriteriaLegacy;
        return ApiResponse.success(requestMasterService.uploadTemplateFile(requestMasterId, file, criteria));
    }

    /**
     * 删除模板文件（软删除）。
     */
    @DeleteMapping("/{requestMasterId}/template-files/{templateFileId}")
    public ApiResponse<Void> deleteTemplateFile(@PathVariable("requestMasterId") Long requestMasterId,
                                                @PathVariable("templateFileId") Long templateFileId) {
        requestMasterService.deleteTemplateFile(requestMasterId, templateFileId);
        return ApiResponse.success();
    }

    /**
     * 版本列表。
     */
    @GetMapping("/{requestMasterId}/versions")
    public ApiResponse<List<RequestMasterVersionListItem>> listVersions(
            @PathVariable("requestMasterId") Long requestMasterId) {
        return ApiResponse.success(requestMasterService.listVersions(requestMasterId));
    }

    /**
     * 保存当前 Individual 快照为新版本。
     */
    @PostMapping("/{requestMasterId}/versions/save")
    public ApiResponse<RequestMasterVersionListItem> saveVersion(
            @PathVariable("requestMasterId") Long requestMasterId) {
        return ApiResponse.success(requestMasterService.saveVersion(requestMasterId));
    }

    /**
     * 版本详情（含 Individual 快照）。
     */
    @GetMapping("/{requestMasterId}/versions/{versionId}")
    public ApiResponse<RequestMasterVersionDetailResponse> getVersionDetail(
            @PathVariable("requestMasterId") Long requestMasterId,
            @PathVariable("versionId") Long versionId) {
        return ApiResponse.success(requestMasterService.getVersionDetail(requestMasterId, versionId));
    }
}
