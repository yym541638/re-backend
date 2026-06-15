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
 * Request Individual 详情侧栏（PRD 2.5.3 / 2.5.4）。
 *
 * <p>从 {@link RequestMasterWorkbenchController} 的 Individual 列表点击进入后，
 * 用于单条 Individual 的表单回显、编辑、发送、证据附件及 Document Owner 选择。</p>
 *
 * <p>路径前缀：{@code /request/individual}；{@code requestId} 为 Individual 数据库主键（数字），
 * 与 Master 列表中的业务编码 {@code request_id}（如 ReqM000001）不同。</p>
 */
@RestController
@RequestMapping("/request/individual")
public class RequestIndividualController {

    private final RequestService requestService;

    public RequestIndividualController(RequestService requestService) {
        this.requestService = requestService;
    }

    /**
     * Request Individual 详情（侧栏表单回显）。
     *
     * <p>GET /request/individual/{requestId}，需 JWT；需具备所属项目的读权限。
     * 返回 requestName、ccCriteria、documentOwner、evidence 列表等完整表单字段。</p>
     */
    @GetMapping("/{requestId}")
    public ApiResponse<RequestIndividualDetailResponse> detail(@PathVariable("requestId") Long requestId) {
        return ApiResponse.success(requestService.individualDetail(requestId));
    }

    /**
     * 保存 Request Individual 表单。
     *
     * <p>PUT /request/individual/{requestId}，需 JWT；需项目写权限。
     * 可更新 requestName、ccCriteria、pointsOfFocus、documentOwner、comment 等字段。</p>
     */
    @PutMapping("/{requestId}")
    public ApiResponse<RequestIndividualDetailResponse> update(
            @PathVariable("requestId") Long requestId,
            @Valid @RequestBody RequestIndividualUpdateRequest request) {
        return ApiResponse.success(requestService.updateIndividual(requestId, request));
    }

    /**
     * 删除 Request Individual（软删除）。
     *
     * <p>DELETE /request/individual/{requestId}，需 JWT；需项目写权限。</p>
     */
    @DeleteMapping("/{requestId}")
    public ApiResponse<Void> delete(@PathVariable("requestId") Long requestId) {
        requestService.delete(requestId);
        return ApiResponse.success();
    }

    /**
     * 发送 Request Individual。
     *
     * <p>POST /request/individual/{requestId}/send，需 JWT；需项目写权限。
     * 记录 requestSendDate，并将 Individual 推送给 Document Owner 处理。</p>
     */
    @PostMapping("/{requestId}/send")
    public ApiResponse<RequestIndividualDetailResponse> send(@PathVariable("requestId") Long requestId) {
        return ApiResponse.success(requestService.sendRequest(requestId));
    }

    /**
     * 查询 Document Owner 候选用户。
     *
     * <p>GET /request/individual/document-owners?projectId=&amp;keyword=，需 JWT。
     * 供表单中 Document Owner 下拉/搜索选择；{@code keyword} 可选，按姓名或邮箱模糊匹配。</p>
     */
    @GetMapping("/document-owners")
    public ApiResponse<List<RequestDocumentOwnerItem>> documentOwners(
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "keyword", required = false) String keyword) {
        return ApiResponse.success(requestService.listDocumentOwners(projectId, keyword));
    }

    /**
     * 上传证据附件。
     *
     * <p>POST /request/individual/{requestId}/attachments，{@code multipart/form-data}，需 JWT。
     * 表单字段：{@code file}（必填）；上传成功后更新 evidence 状态并同步 RCM 草稿。</p>
     */
    @PostMapping("/{requestId}/attachments")
    public ApiResponse<RequestAttachment> uploadAttachment(@PathVariable("requestId") Long requestId,
                                                           @RequestParam("file") MultipartFile file) {
        return ApiResponse.success(requestService.uploadAttachment(requestId, file));
    }

    /**
     * 重命名证据附件。
     *
     * <p>PUT /request/individual/{requestId}/attachments/{attachmentId}，需 JWT。
     * 请求体 {@link RequestEvidenceRenameRequest} 含新文件名。</p>
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
     *
     * <p>DELETE /request/individual/{requestId}/attachments/{attachmentId}，需 JWT。</p>
     */
    @DeleteMapping("/{requestId}/attachments/{attachmentId}")
    public ApiResponse<Void> deleteAttachment(@PathVariable("requestId") Long requestId,
                                            @PathVariable("attachmentId") Long attachmentId) {
        requestService.deleteAttachment(requestId, attachmentId);
        return ApiResponse.success();
    }
}
