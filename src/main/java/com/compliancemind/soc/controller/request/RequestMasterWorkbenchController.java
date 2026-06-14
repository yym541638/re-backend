package com.compliancemind.soc.controller.request;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.common.api.PageResponse;
import com.compliancemind.soc.dto.request.RequestIndividualCreateRequest;
import com.compliancemind.soc.dto.request.RequestIndividualDetailResponse;
import com.compliancemind.soc.dto.request.RequestIndividualListItem;
import com.compliancemind.soc.dto.request.RequestIndividualUpdateRequest;
import com.compliancemind.soc.dto.request.RequestMasterTemplateFileItem;
import com.compliancemind.soc.dto.request.RequestMasterVersionDetailResponse;
import com.compliancemind.soc.dto.request.RequestMasterVersionListItem;
import com.compliancemind.soc.service.request.RequestMasterService;
import com.compliancemind.soc.service.request.RequestService;
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
 * Request Master 工作台：Individual 列表、模板文件、版本、Run Generate。
 */
@RestController
@RequestMapping("/request-master")
public class RequestMasterWorkbenchController {

    private final RequestMasterService requestMasterService;
    private final RequestService requestService;

    public RequestMasterWorkbenchController(RequestMasterService requestMasterService,
                                            RequestService requestService) {
        this.requestMasterService = requestMasterService;
        this.requestService = requestService;
    }

    @GetMapping("/{requestMasterId}/individuals")
    public ApiResponse<List<RequestIndividualListItem>> listIndividuals(
            @PathVariable("requestMasterId") Long requestMasterId) {
        return ApiResponse.success(requestMasterService.listIndividuals(requestMasterId));
    }

    @PostMapping("/{requestMasterId}/individuals")
    public ApiResponse<RequestIndividualDetailResponse> createIndividual(
            @PathVariable("requestMasterId") Long requestMasterId,
            @Valid @RequestBody RequestIndividualCreateRequest request) {
        request.setRequestMasterId(requestMasterId);
        return ApiResponse.success(requestService.createIndividual(request));
    }

    @PostMapping("/{requestMasterId}/generate")
    public ApiResponse<List<RequestIndividualListItem>> generateIndividuals(
            @PathVariable("requestMasterId") Long requestMasterId) {
        return ApiResponse.success(requestMasterService.generateIndividuals(requestMasterId));
    }

    @GetMapping("/{requestMasterId}/template-files")
    public ApiResponse<PageResponse<RequestMasterTemplateFileItem>> listTemplateFiles(
            @PathVariable("requestMasterId") Long requestMasterId,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        return ApiResponse.page(requestMasterService.listTemplateFiles(requestMasterId, pageNum, pageSize));
    }

    @PostMapping(value = "/{requestMasterId}/template-files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<RequestMasterTemplateFileItem> uploadTemplateFile(
            @PathVariable("requestMasterId") Long requestMasterId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "relevantCriteria", required = false) String relevantCriteria,
            @RequestParam(value = "relevant_criteria", required = false) String relevantCriteriaLegacy) {
        String criteria = relevantCriteria != null ? relevantCriteria : relevantCriteriaLegacy;
        return ApiResponse.success(requestMasterService.uploadTemplateFile(requestMasterId, file, criteria));
    }

    @DeleteMapping("/{requestMasterId}/template-files/{templateFileId}")
    public ApiResponse<Void> deleteTemplateFile(@PathVariable("requestMasterId") Long requestMasterId,
                                                @PathVariable("templateFileId") Long templateFileId) {
        requestMasterService.deleteTemplateFile(requestMasterId, templateFileId);
        return ApiResponse.success();
    }

    @GetMapping("/template-files/download-template")
    public ResponseEntity<byte[]> downloadTemplate() {
        byte[] content = requestMasterService.downloadTemplateFile();
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"request_individual_template.csv\"")
            .contentType(MediaType.parseMediaType("text/csv"))
            .body(content);
    }

    @GetMapping("/{requestMasterId}/versions")
    public ApiResponse<List<RequestMasterVersionListItem>> listVersions(
            @PathVariable("requestMasterId") Long requestMasterId) {
        return ApiResponse.success(requestMasterService.listVersions(requestMasterId));
    }

    @PostMapping("/{requestMasterId}/versions/save")
    public ApiResponse<RequestMasterVersionListItem> saveVersion(
            @PathVariable("requestMasterId") Long requestMasterId) {
        return ApiResponse.success(requestMasterService.saveVersion(requestMasterId));
    }

    @GetMapping("/{requestMasterId}/versions/{versionId}")
    public ApiResponse<RequestMasterVersionDetailResponse> getVersionDetail(
            @PathVariable("requestMasterId") Long requestMasterId,
            @PathVariable("versionId") Long versionId) {
        return ApiResponse.success(requestMasterService.getVersionDetail(requestMasterId, versionId));
    }
}
