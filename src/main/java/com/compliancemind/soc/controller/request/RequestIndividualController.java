package com.compliancemind.soc.controller.request;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.dto.request.RequestDocumentOwnerItem;
import com.compliancemind.soc.dto.request.RequestEvidenceItem;
import com.compliancemind.soc.dto.request.RequestEvidenceRenameRequest;
import com.compliancemind.soc.dto.request.RequestIndividualDetailResponse;
import com.compliancemind.soc.dto.request.RequestIndividualUpdateRequest;
import com.compliancemind.soc.entity.request.RequestAttachment;
import com.compliancemind.soc.service.request.RequestService;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Request Individual 侧栏表单（PRD 2.5.3 / 2.5.4）。
 */
@RestController
@RequestMapping("/request/individual")
public class RequestIndividualController {

    private final RequestService requestService;

    public RequestIndividualController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping("/{requestId}")
    public ApiResponse<RequestIndividualDetailResponse> detail(@PathVariable("requestId") Long requestId) {
        return ApiResponse.success(requestService.individualDetail(requestId));
    }

    @PutMapping("/{requestId}")
    public ApiResponse<RequestIndividualDetailResponse> update(
            @PathVariable("requestId") Long requestId,
            @Valid @RequestBody RequestIndividualUpdateRequest request) {
        return ApiResponse.success(requestService.updateIndividual(requestId, request));
    }

    @DeleteMapping("/{requestId}")
    public ApiResponse<Void> delete(@PathVariable("requestId") Long requestId) {
        requestService.delete(requestId);
        return ApiResponse.success();
    }

    @PostMapping("/{requestId}/send")
    public ApiResponse<RequestIndividualDetailResponse> send(@PathVariable("requestId") Long requestId) {
        return ApiResponse.success(requestService.sendRequest(requestId));
    }

    @GetMapping("/document-owners")
    public ApiResponse<List<RequestDocumentOwnerItem>> documentOwners(
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.success(requestService.listDocumentOwners(projectId, keyword));
    }

    @PostMapping("/{requestId}/attachments")
    public ApiResponse<RequestAttachment> uploadAttachment(@PathVariable("requestId") Long requestId,
                                                           @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(requestService.uploadAttachment(requestId, file));
    }

    @PutMapping("/{requestId}/attachments/{attachmentId}")
    public ApiResponse<RequestEvidenceItem> renameAttachment(
            @PathVariable("requestId") Long requestId,
            @PathVariable("attachmentId") Long attachmentId,
            @Valid @RequestBody RequestEvidenceRenameRequest request) {
        return ApiResponse.success(requestService.renameAttachment(requestId, attachmentId, request));
    }

    @DeleteMapping("/{requestId}/attachments/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@PathVariable("requestId") Long requestId,
                                            @PathVariable("attachmentId") Long attachmentId) {
        requestService.deleteAttachment(requestId, attachmentId);
        return ApiResponse.success();
    }
}
