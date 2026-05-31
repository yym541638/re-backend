package com.compliancemind.soc.controller.request;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.dto.request.RequestCreateRequest;

import com.compliancemind.soc.dto.request.RequestDetailResponse;

import com.compliancemind.soc.dto.request.RequestQueryRequest;

import com.compliancemind.soc.dto.request.RequestUpdateRequest;

import com.compliancemind.soc.dto.request.RequestVersionCreateRequest;

import com.compliancemind.soc.entity.request.ComplianceRequest;

import com.compliancemind.soc.entity.request.RequestAttachment;

import com.compliancemind.soc.entity.request.RequestVersion;

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



/**

 * 合规请求（Request）：列表、详情、增删改、版本快照、附件上传/删除（含旧版路径别名）。

 * <p>对应 PRD 2.5.2 Request Master、2.5.3 单项目询问管理；2.5.4 详情页接口见附录 C。</p>

 */

@RestController

@RequestMapping("/request")

public class RequestController {



    private final RequestService requestService;



    public RequestController(RequestService requestService) {

        this.requestService = requestService;

    }



    /**

     * 询问列表（PRD 2.5.2 / 2.5.3）。

     * <p>GET /request/list，需 JWT；projectId 必填，可按 documentStatus、ccCriteria 筛选。</p>

     *

     * @param projectId            项目 ID（camelCase）

     * @param projectIdLegacy      项目 ID（snake_case 别名）

     * @param documentStatus       文档状态（camelCase）

     * @param documentStatusLegacy 文档状态（status 别名）

     * @param ccCriteria           CC 标准（camelCase）

     * @param ccCriteriaLegacy     CC 标准（type 别名）

     * @return 合规请求列表

     */

    @GetMapping("/list")

    public ApiResponse<java.util.List<ComplianceRequest>> list(@RequestParam(value = "projectId", required = false) Long projectId,

                                                             @RequestParam(value = "project_id", required = false) Long projectIdLegacy,

                                                             @RequestParam(value = "documentStatus", required = false) String documentStatus,

                                                             @RequestParam(value = "status", required = false) String documentStatusLegacy,

                                                             @RequestParam(value = "ccCriteria", required = false) String ccCriteria,

                                                             @RequestParam(value = "type", required = false) String ccCriteriaLegacy) {

        RequestQueryRequest request = new RequestQueryRequest();

        request.setProjectId(projectId != null ? projectId : projectIdLegacy);

        request.setDocumentStatus(documentStatus != null ? documentStatus : documentStatusLegacy);

        request.setCcCriteria(ccCriteria != null ? ccCriteria : ccCriteriaLegacy);

        return ApiResponse.success(requestService.list(request));

    }



    /**

     * 请求详情（PRD 2.5.4 参考 / 附录 C）。

     * <p>GET /request/{requestId}，需 JWT；含附件列表及版本历史。</p>

     *

     * @param requestId 请求 ID

     * @return 请求详情、附件及版本

     */

    @GetMapping("/{requestId}")

    public ApiResponse<RequestDetailResponse> detail(@PathVariable("requestId") Long requestId) {

        return ApiResponse.success(requestService.detail(requestId));

    }



    /**

     * 请求详情（旧版兼容路径）。

     * <p>GET /request/detail/{request_id}，行为与 {@link #detail(Long)} 一致。</p>

     *

     * @param requestId 请求 ID

     * @return 请求详情、附件及版本

     */

    @GetMapping("/detail/{request_id}")

    public ApiResponse<RequestDetailResponse> legacyDetail(@PathVariable("request_id") Long requestId) {

        return ApiResponse.success(requestService.detail(requestId));

    }



    /**

     * 新建询问行（PRD 2.5.2）。

     * <p>POST /request，需 JWT。</p>

     *

     * @param request 请求创建字段（projectId、ccCriteria、title 等）

     * @return 新建的合规请求

     */

    @PostMapping

    public ApiResponse<ComplianceRequest> create(@Valid @RequestBody RequestCreateRequest request) {

        return ApiResponse.success(requestService.create(request));

    }



    /**

     * 新建询问行（旧版兼容路径）。

     * <p>POST /request/create，行为与 {@link #create(RequestCreateRequest)} 一致。</p>

     *

     * @param request 请求创建字段

     * @return 新建的合规请求

     */

    @PostMapping("/create")

    public ApiResponse<ComplianceRequest> legacyCreate(@Valid @RequestBody RequestCreateRequest request) {

        return ApiResponse.success(requestService.create(request));

    }



    /**

     * 编辑询问行（PRD 2.5.2 / 2.5.3）。

     * <p>PUT /request/{requestId}，需 JWT。</p>

     *

     * @param requestId 请求 ID

     * @param request   更新字段

     * @return 更新后的合规请求

     */

    @PutMapping("/{requestId}")

    public ApiResponse<ComplianceRequest> update(@PathVariable("requestId") Long requestId,

                                                 @Valid @RequestBody RequestUpdateRequest request) {

        return ApiResponse.success(requestService.update(requestId, request));

    }



    /**

     * 编辑询问行（旧版兼容路径）。

     * <p>PUT /request/update/{request_id}，行为与 {@link #update(Long, RequestUpdateRequest)} 一致。</p>

     *

     * @param requestId 请求 ID

     * @param request   更新字段

     * @return 更新后的合规请求

     */

    @PutMapping("/update/{request_id}")

    public ApiResponse<ComplianceRequest> legacyUpdate(@PathVariable("request_id") Long requestId,

                                                       @Valid @RequestBody RequestUpdateRequest request) {

        return ApiResponse.success(requestService.update(requestId, request));

    }



    /**

     * 保存请求版本（PRD 2.5.4 参考 / 附录 C）。

     * <p>POST /request/{requestId}/versions，需 JWT；保存当前数据快照。</p>

     *

     * @param requestId 请求 ID

     * @param request   版本备注/变更摘要

     * @return 新建的版本记录

     */

    @PostMapping("/{requestId}/versions")

    public ApiResponse<RequestVersion> saveVersion(@PathVariable("requestId") Long requestId,

                                                   @Valid @RequestBody RequestVersionCreateRequest request) {

        return ApiResponse.success(requestService.saveVersion(requestId, request));

    }



    /**

     * 保存请求版本（旧版兼容路径）。

     * <p>POST /request/{request_id}/save-version，行为与 {@link #saveVersion(Long, RequestVersionCreateRequest)} 一致。</p>

     *

     * @param requestId 请求 ID

     * @param request   版本备注/变更摘要

     * @return 新建的版本记录

     */

    @PostMapping("/{request_id}/save-version")

    public ApiResponse<RequestVersion> legacySaveVersion(@PathVariable("request_id") Long requestId,

                                                         @Valid @RequestBody RequestVersionCreateRequest request) {

        return ApiResponse.success(requestService.saveVersion(requestId, request));

    }



    /**

     * 编辑并保存请求（旧版兼容路径）。

     * <p>PUT /request/save/{request_id}，行为与 {@link #update(Long, RequestUpdateRequest)} 一致。</p>

     *

     * @param requestId 请求 ID

     * @param request   更新字段

     * @return 更新后的合规请求

     */

    @PutMapping("/save/{request_id}")

    public ApiResponse<ComplianceRequest> legacySave(@PathVariable("request_id") Long requestId,

                                                     @Valid @RequestBody RequestUpdateRequest request) {

        return ApiResponse.success(requestService.update(requestId, request));

    }



    /**

     * 上传请求附件（PRD 2.5.4 参考 / 附录 C）。

     * <p>POST /request/{requestId}/attachments，需 JWT；支持 pdf/excel/word。</p>

     *

     * @param requestId 请求 ID

     * @param file      上传文件（multipart/form-data）

     * @return 附件元数据

     */

    @PostMapping("/{requestId}/attachments")

    public ApiResponse<RequestAttachment> uploadAttachment(@PathVariable("requestId") Long requestId,

                                                           @RequestParam("file") MultipartFile file) {

        return ApiResponse.success(requestService.uploadAttachment(requestId, file));

    }



    /**

     * 附件列表（旧版兼容路径）。

     * <p>GET /request/{request_id}/documents，返回详情中的 attachments 字段。</p>

     *

     * @param requestId 请求 ID

     * @return 附件列表

     */

    @GetMapping("/{request_id}/documents")

    public ApiResponse<java.util.List<RequestAttachment>> legacyDocuments(@PathVariable("request_id") Long requestId) {

        return ApiResponse.success(requestService.detail(requestId).getAttachments());

    }



    /**

     * 上传附件（旧版兼容路径）。

     * <p>POST /request/{request_id}/document/upload，行为与 {@link #uploadAttachment(Long, MultipartFile)} 一致。</p>

     *

     * @param requestId 请求 ID

     * @param file      上传文件

     * @return 附件元数据

     */

    @PostMapping("/{request_id}/document/upload")

    public ApiResponse<RequestAttachment> legacyUpload(@PathVariable("request_id") Long requestId,

                                                       @RequestParam("file") MultipartFile file) {

        return ApiResponse.success(requestService.uploadAttachment(requestId, file));

    }



    /**

     * 删除请求附件（PRD 2.5.4 参考 / 附录 C）。

     * <p>DELETE /request/{requestId}/attachments/{attachmentId}，需 JWT；软删除。</p>

     *

     * @param requestId    请求 ID

     * @param attachmentId 附件 ID

     * @return 操作成功空响应

     */

    @DeleteMapping("/{requestId}/attachments/{attachmentId}")

    public ApiResponse<Void> deleteAttachment(@PathVariable("requestId") Long requestId,

                                              @PathVariable("attachmentId") Long attachmentId) {

        requestService.deleteAttachment(requestId, attachmentId);

        return ApiResponse.success();

    }



    /**

     * 删除附件（旧版兼容路径）。

     * <p>DELETE /request/document/{document_id}，按附件 ID 删除。</p>

     *

     * @param attachmentId 附件 ID

     * @return 操作成功空响应

     */

    @DeleteMapping("/document/{document_id}")

    public ApiResponse<Void> legacyDeleteAttachment(@PathVariable("document_id") Long attachmentId) {

        requestService.deleteAttachmentById(attachmentId);

        return ApiResponse.success();

    }



    /**

     * 删除询问行（PRD 2.5.2 / 2.5.3）。

     * <p>DELETE /request/{requestId}，需 JWT；软删除。</p>

     *

     * @param requestId 请求 ID

     * @return 操作成功空响应

     */

    @DeleteMapping("/{requestId}")

    public ApiResponse<Void> delete(@PathVariable("requestId") Long requestId) {

        requestService.delete(requestId);

        return ApiResponse.success();

    }

}


