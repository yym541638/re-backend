package com.compliancemind.soc.controller.request;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.common.api.PageResponse;

import com.compliancemind.soc.dto.request.RequestIndividualCreateRequest;

import com.compliancemind.soc.dto.request.RequestIndividualDetailResponse;

import com.compliancemind.soc.dto.request.RequestIndividualListItem;

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

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;



import java.util.List;



/**

 * Request Master 工作台（PRD 2.5.3）：从 Master 列表点击进入后的 Request Individual 页面。

 *

 * <p>提供 Individual 列表、Run Generate、模板文件及版本快照等接口；单条 Individual 详情见

 * {@link RequestIndividualController}。</p>

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



    /**

     * Request Individual 列表。

     *

     * <p>GET /request-master/{requestMasterId}/individuals，需 JWT。

     * 从 Request Master 点击进入后加载表格数据。</p>

     */

    @GetMapping("/{requestMasterId}/individuals")

    public ApiResponse<List<RequestIndividualListItem>> listIndividuals(

            @PathVariable("requestMasterId") Long requestMasterId) {

        return ApiResponse.success(requestMasterService.listIndividuals(requestMasterId));

    }



    /**

     * 新建 Request Individual。

     *

     * <p>POST /request-master/{requestMasterId}/individuals，需 JWT；需项目写权限。

     * 路径中的 {@code requestMasterId} 会写入请求体。</p>

     */

    @PostMapping("/{requestMasterId}/individuals")

    public ApiResponse<RequestIndividualDetailResponse> createIndividual(

            @PathVariable("requestMasterId") Long requestMasterId,

            @Valid @RequestBody RequestIndividualCreateRequest request) {

        request.setRequestMasterId(requestMasterId);

        return ApiResponse.success(requestService.createIndividual(request));

    }



    /**

     * Run Generate：根据已上传模板批量生成 Individual。

     *

     * <p>POST /request-master/{requestMasterId}/generate，需 JWT；需项目写权限。

     * 返回生成后的 Individual 列表。</p>

     */

    @PostMapping("/{requestMasterId}/generate")

    public ApiResponse<List<RequestIndividualListItem>> generateIndividuals(

            @PathVariable("requestMasterId") Long requestMasterId) {

        return ApiResponse.success(requestMasterService.generateIndividuals(requestMasterId));

    }



    /**

     * 模板文件列表（分页）。

     *

     * <p>GET /request-master/{requestMasterId}/template-files?pageNum=&amp;pageSize=，需 JWT。</p>

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

     *

     * <p>POST /request-master/{requestMasterId}/template-files，{@code multipart/form-data}，需 JWT。

     * 表单字段：{@code file}（必填）、{@code relevantCriteria} / {@code relevant_criteria}（可选）。</p>

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

     *

     * <p>DELETE /request-master/{requestMasterId}/template-files/{templateFileId}，需 JWT。</p>

     */

    @DeleteMapping("/{requestMasterId}/template-files/{templateFileId}")

    public ApiResponse<Void> deleteTemplateFile(@PathVariable("requestMasterId") Long requestMasterId,

                                                @PathVariable("templateFileId") Long templateFileId) {

        requestMasterService.deleteTemplateFile(requestMasterId, templateFileId);

        return ApiResponse.success();

    }



    /**

     * 下载 Individual 批量导入 CSV 模板。

     *

     * <p>GET /request-master/template-files/download-template，需 JWT；返回 {@code request_individual_template.csv}。</p>

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

     * Request Master 版本列表。

     *

     * <p>GET /request-master/{requestMasterId}/versions，需 JWT；供版本下拉/历史列表使用。</p>

     */

    @GetMapping("/{requestMasterId}/versions")

    public ApiResponse<List<RequestMasterVersionListItem>> listVersions(

            @PathVariable("requestMasterId") Long requestMasterId) {

        return ApiResponse.success(requestMasterService.listVersions(requestMasterId));

    }



    /**

     * 保存当前 Individual 快照为新版本。

     *

     * <p>POST /request-master/{requestMasterId}/versions/save，需 JWT；需项目写权限。

     * 将当前 Individual 列表序列化为版本快照并标记为最新版本。</p>

     */

    @PostMapping("/{requestMasterId}/versions/save")

    public ApiResponse<RequestMasterVersionListItem> saveVersion(

            @PathVariable("requestMasterId") Long requestMasterId) {

        return ApiResponse.success(requestMasterService.saveVersion(requestMasterId));

    }



    /**

     * 版本详情（含 Individual 快照）。

     *

     * <p>GET /request-master/{requestMasterId}/versions/{versionId}，需 JWT；

     * 用于查看历史版本下的 Individual 列表。</p>

     */

    @GetMapping("/{requestMasterId}/versions/{versionId}")

    public ApiResponse<RequestMasterVersionDetailResponse> getVersionDetail(

            @PathVariable("requestMasterId") Long requestMasterId,

            @PathVariable("versionId") Long versionId) {

        return ApiResponse.success(requestMasterService.getVersionDetail(requestMasterId, versionId));

    }

}


