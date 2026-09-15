package com.compliancemind.soc.controller.request;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.dto.request.RequestDocumentOwnerItem;
import com.compliancemind.soc.dto.request.RequestEvidenceItem;
import com.compliancemind.soc.dto.request.RequestEvidenceRenameRequest;
import com.compliancemind.soc.dto.request.RequestIndividualCreateRequest;
import com.compliancemind.soc.dto.request.RequestIndividualDetailResponse;
import com.compliancemind.soc.dto.request.RequestIndividualListItem;
import com.compliancemind.soc.dto.request.RequestIndividualUpdateRequest;
import com.compliancemind.soc.entity.request.RequestAttachment;
import com.compliancemind.soc.service.request.RequestMasterService;
import com.compliancemind.soc.service.request.RequestService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
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

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Request Individual：某个 Request Master 下的条目列表、生成、单条详情与证据。
 *
 * <p>路径前缀 {@code /request/individual}。Master 本身的 CRUD 见 {@link RequestMasterController}。</p>
 */
@RestController
@RequestMapping("/request/individual")
public class RequestIndividualController {

    private final RequestService requestService;
    private final RequestMasterService requestMasterService;

    public RequestIndividualController(RequestService requestService,
                                       RequestMasterService requestMasterService) {
        this.requestService = requestService;
        this.requestMasterService = requestMasterService;
    }

    /**
     * 某个 Request Master 下的 Individual 列表。
     *
     * <p>GET /request/individual/list?requestMasterId=</p>
     */
    @GetMapping("/list")
    public ApiResponse<List<RequestIndividualListItem>> list(
            @RequestParam("requestMasterId") Long requestMasterId) {
        return ApiResponse.success(requestMasterService.listIndividuals(requestMasterId));
    }

    /**
     * 新建 Request Individual。
     *
     * <p>POST /request/individual，Body 需含 requestMasterId。</p>
     */
    @PostMapping
    public ApiResponse<RequestIndividualDetailResponse> create(
            @Valid @RequestBody RequestIndividualCreateRequest request) {
        return ApiResponse.success(requestService.createIndividual(request));
    }

    /**
     * 按已购套餐范围，从标准条款库生成单条材料任务。
     *
     * <p>POST /request/individual/generate?requestMasterId=</p>
     */
    @PostMapping("/generate")
    public ApiResponse<List<RequestIndividualListItem>> generate(
            @RequestParam("requestMasterId") Long requestMasterId) {
        return ApiResponse.success(requestMasterService.generateIndividuals(requestMasterId));
    }

    /**
     * Document Owner 候选用户。
     *
     * <p>GET /request/individual/document-owners?projectId=&amp;keyword=</p>
     */
    @GetMapping("/document-owners")
    public ApiResponse<List<RequestDocumentOwnerItem>> documentOwners(
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.success(requestService.listDocumentOwners(projectId, keyword));
    }

    /**
     * 单条详情（表单回显）。
     */
    @GetMapping("/{requestId}")
    public ApiResponse<RequestIndividualDetailResponse> detail(@PathVariable("requestId") Long requestId) {
        return ApiResponse.success(requestService.individualDetail(requestId));
    }

    /**
     * 保存单条表单。
     */
    @PutMapping("/{requestId}")
    public ApiResponse<RequestIndividualDetailResponse> update(
            @PathVariable("requestId") Long requestId,
            @Valid @RequestBody RequestIndividualUpdateRequest request) {
        return ApiResponse.success(requestService.updateIndividual(requestId, request));
    }

    /**
     * 删除单条（软删除）。
     */
    @DeleteMapping("/{requestId}")
    public ApiResponse<Void> delete(@PathVariable("requestId") Long requestId) {
        requestService.delete(requestId);
        return ApiResponse.success();
    }

    /**
     * 发送单条 Request。
     */
    @PostMapping("/{requestId}/send")
    public ApiResponse<RequestIndividualDetailResponse> send(@PathVariable("requestId") Long requestId) {
        return ApiResponse.success(requestService.sendRequest(requestId));
    }

    /**
     * 上传证据附件。
     */
    @PostMapping("/{requestId}/attachments")
    public ApiResponse<RequestAttachment> uploadAttachment(@PathVariable("requestId") Long requestId,
                                                           @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(requestService.uploadAttachment(requestId, file));
    }

    /**
     * 重命名证据附件。
     */
    @PutMapping("/{requestId}/attachments/{attachmentId}")
    public ApiResponse<RequestEvidenceItem> renameAttachment(
            @PathVariable("requestId") Long requestId,
            @PathVariable("attachmentId") Long attachmentId,
            @Valid @RequestBody RequestEvidenceRenameRequest request) {
        return ApiResponse.success(requestService.renameAttachment(requestId, attachmentId, request));
    }

    /**
     * 删除证据附件（软删除）。
     */
    @DeleteMapping("/{requestId}/attachments/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@PathVariable("requestId") Long requestId,
                                              @PathVariable("attachmentId") Long attachmentId) {
        requestService.deleteAttachment(requestId, attachmentId);
        return ApiResponse.success();
    }

    /**
     * View / download 证据附件。
     *
     * <p>GET /request/individual/{requestId}/attachments/{attachmentId}/download</p>
     */
    @GetMapping(value = "/{requestId}/attachments/{attachmentId}/download",
        produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable("requestId") Long requestId,
                                                     @PathVariable("attachmentId") Long attachmentId) {
        RequestService.AttachmentDownload download =
            requestService.downloadAttachment(requestId, attachmentId);
        String fileName = download.fileName();
        if (fileName == null || fileName.isBlank()) {
            fileName = "evidence";
        }
        ContentDisposition disposition = ContentDisposition.inline()
            .filename(fileName, StandardCharsets.UTF_8)
            .build();
        byte[] body = download.content() == null ? new byte[0] : download.content();
        // 必须用 octet-stream：text/plain 等类型会走 StringHttpMessageConverter，byte[] 无法写出 → 500
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .contentLength(body.length)
            .body(body);
    }
}
