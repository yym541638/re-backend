package com.compliancemind.soc.controller.rcm;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.common.constants.SocConstants;

import com.compliancemind.soc.dto.rcm.RcmAiGenerateRequest;

import com.compliancemind.soc.dto.rcm.RcmCreateRequest;

import com.compliancemind.soc.dto.rcm.RcmDetailResponse;

import com.compliancemind.soc.dto.rcm.RcmImportResultResponse;

import com.compliancemind.soc.dto.rcm.RcmQueryRequest;

import com.compliancemind.soc.dto.rcm.RcmUpdateRequest;

import com.compliancemind.soc.dto.rcm.RcmVersionCreateRequest;

import com.compliancemind.soc.entity.rcm.RcmRecord;

import com.compliancemind.soc.entity.rcm.RcmVersion;

import com.compliancemind.soc.service.rcm.RcmService;

import jakarta.servlet.http.HttpServletResponse;

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



import java.io.IOException;

import java.net.URLEncoder;

import java.nio.charset.StandardCharsets;

import java.util.List;

import java.util.Map;



/**

 * RCM（风险控制矩阵）：按阶段列表、详情、版本、Excel 导入导出、AI 辅助填写与晋级等。

 * <p>对应 PRD 2.5.5 RCM Final、2.5.6 RCM Manual、2.5.7 AI Generate RCM。</p>

 */

@RestController

@RequestMapping("/rcm")

public class RcmController {



    private final RcmService rcmService;



    public RcmController(RcmService rcmService) {

        this.rcmService = rcmService;

    }



    /**

     * RCM 通用列表。

     * <p>GET /rcm/list，需 JWT；projectId 必填，可按 stage/status/category 等筛选。</p>

     *

     * @param request              查询条件

     * @param projectIdLegacy      项目 ID（snake_case 别名）

     * @param categoryLegacy       模块/CC（module 别名）

     * @param riskRatingLegacy     风险等级（snake_case 别名）

     * @param sourceRequestIdLegacy 来源 Request ID（snake_case 别名）

     * @return RCM 记录列表

     */

    @GetMapping("/list")

    public ApiResponse<List<RcmRecord>> list(RcmQueryRequest request,

                                             @RequestParam(value = "project_id", required = false) Long projectIdLegacy,

                                             @RequestParam(value = "module", required = false) String categoryLegacy,

                                             @RequestParam(value = "risk_rating", required = false) String riskRatingLegacy,

                                             @RequestParam(value = "source_request_id", required = false) Long sourceRequestIdLegacy) {

        applyLegacyQuery(request, projectIdLegacy, categoryLegacy, riskRatingLegacy, sourceRequestIdLegacy, request.getStage());

        return ApiResponse.success(rcmService.list(request));

    }



    /**

     * Manual 阶段列表（PRD 2.5.6）。

     * <p>GET /rcm/manual/list，需 JWT；固定 stage=MANUAL。</p>

     *

     * @param request              查询条件

     * @param projectIdLegacy      项目 ID（snake_case 别名）

     * @param categoryLegacy       模块/CC（module 别名）

     * @param riskRatingLegacy     风险等级（snake_case 别名）

     * @param sourceRequestIdLegacy 来源 Request ID（snake_case 别名）

     * @return Manual 阶段 RCM 列表

     */

    @GetMapping("/manual/list")

    public ApiResponse<List<RcmRecord>> manualList(RcmQueryRequest request,

                                                   @RequestParam(value = "project_id", required = false) Long projectIdLegacy,

                                                   @RequestParam(value = "module", required = false) String categoryLegacy,

                                                   @RequestParam(value = "risk_rating", required = false) String riskRatingLegacy,

                                                   @RequestParam(value = "source_request_id", required = false) Long sourceRequestIdLegacy) {

        applyLegacyQuery(request, projectIdLegacy, categoryLegacy, riskRatingLegacy, sourceRequestIdLegacy, SocConstants.Rcm.STAGE_MANUAL);

        return ApiResponse.success(rcmService.list(request));

    }



    /**

     * Final 阶段列表（PRD 2.5.5）。

     * <p>GET /rcm/final/list，需 JWT；固定 stage=FINAL。</p>

     *

     * @param request              查询条件

     * @param projectIdLegacy      项目 ID（snake_case 别名）

     * @param categoryLegacy       模块/CC（module 别名）

     * @param riskRatingLegacy     风险等级（snake_case 别名）

     * @param sourceRequestIdLegacy 来源 Request ID（snake_case 别名）

     * @return Final 阶段 RCM 列表

     */

    @GetMapping("/final/list")

    public ApiResponse<List<RcmRecord>> finalList(RcmQueryRequest request,

                                                  @RequestParam(value = "project_id", required = false) Long projectIdLegacy,

                                                  @RequestParam(value = "module", required = false) String categoryLegacy,

                                                  @RequestParam(value = "risk_rating", required = false) String riskRatingLegacy,

                                                  @RequestParam(value = "source_request_id", required = false) Long sourceRequestIdLegacy) {

        applyLegacyQuery(request, projectIdLegacy, categoryLegacy, riskRatingLegacy, sourceRequestIdLegacy, SocConstants.Rcm.STAGE_FINAL);

        return ApiResponse.success(rcmService.list(request));

    }



    /**

     * AI 生成阶段列表（PRD 2.5.7）。

     * <p>GET /rcm/ai-generated/list，需 JWT；固定 stage=AI_GENERATED。</p>

     *

     * @param request              查询条件

     * @param projectIdLegacy      项目 ID（snake_case 别名）

     * @param categoryLegacy       模块/CC（module 别名）

     * @param riskRatingLegacy     风险等级（snake_case 别名）

     * @param sourceRequestIdLegacy 来源 Request ID（snake_case 别名）

     * @return AI 生成阶段 RCM 列表

     */

    @GetMapping("/ai-generated/list")

    public ApiResponse<List<RcmRecord>> aiGeneratedList(RcmQueryRequest request,

                                                        @RequestParam(value = "project_id", required = false) Long projectIdLegacy,

                                                        @RequestParam(value = "module", required = false) String categoryLegacy,

                                                        @RequestParam(value = "risk_rating", required = false) String riskRatingLegacy,

                                                        @RequestParam(value = "source_request_id", required = false) Long sourceRequestIdLegacy) {

        applyLegacyQuery(request, projectIdLegacy, categoryLegacy, riskRatingLegacy, sourceRequestIdLegacy, SocConstants.Rcm.STAGE_AI_GENERATED);

        return ApiResponse.success(rcmService.list(request));

    }



    /**

     * RCM 详情（PRD 2.5.5 / 2.5.6 / 2.5.7）。

     * <p>GET /rcm/{rcmId}，需 JWT；含版本历史。</p>

     *

     * @param rcmId RCM 记录 ID

     * @return RCM 详情及版本列表

     */

    @GetMapping("/{rcmId}")

    public ApiResponse<RcmDetailResponse> detail(@PathVariable("rcmId") Long rcmId) {

        return ApiResponse.success(rcmService.detail(rcmId));

    }



    /**

     * RCM 版本列表（旧版兼容路径）。

     * <p>GET /rcm/{rcm_id}/versions，需 JWT；等价于详情中的 versions 字段。</p>

     *

     * @param rcmId RCM 记录 ID

     * @return 版本列表

     */

    @GetMapping("/{rcm_id}/versions")

    public ApiResponse<List<RcmVersion>> legacyVersions(@PathVariable("rcm_id") Long rcmId) {

        return ApiResponse.success(rcmService.detail(rcmId).getVersions());

    }



    /**

     * AI 服务状态（PRD 2.5.7）。

     * <p>GET /rcm/ai/status，需 JWT；查询 AI 生成服务是否可用。</p>

     *

     * @return AI 服务状态信息

     */

    @GetMapping("/ai/status")

    public ApiResponse<Map<String, Object>> aiStatus() {

        return ApiResponse.success(rcmService.aiStatus());

    }



    /**

     * 新建 RCM 行（PRD 2.5.6）。

     * <p>POST /rcm，需 JWT；Manual 阶段新建控制矩阵行。</p>

     *

     * @param request 控制编号、名称、描述、阶段等

     * @return 新建的 RCM 记录

     */

    @PostMapping

    public ApiResponse<RcmRecord> create(@Valid @RequestBody RcmCreateRequest request) {

        return ApiResponse.success(rcmService.create(request));

    }



    /**

     * 新建 RCM 行（旧版兼容路径）。

     * <p>POST /rcm/create，行为与 {@link #create(RcmCreateRequest)} 一致。</p>

     *

     * @param request 控制矩阵创建字段

     * @return 新建的 RCM 记录

     */

    @PostMapping("/create")

    public ApiResponse<RcmRecord> legacyCreate(@Valid @RequestBody RcmCreateRequest request) {

        return ApiResponse.success(rcmService.create(request));

    }



    /**

     * 编辑 RCM 行（PRD 2.5.5 / 2.5.6）。

     * <p>PUT /rcm/{rcmId}，需 JWT。</p>

     *

     * @param rcmId   RCM 记录 ID

     * @param request 更新字段

     * @return 更新后的 RCM 记录

     */

    @PutMapping("/{rcmId}")

    public ApiResponse<RcmRecord> update(@PathVariable("rcmId") Long rcmId,

                                         @Valid @RequestBody RcmUpdateRequest request) {

        return ApiResponse.success(rcmService.update(rcmId, request));

    }



    /**

     * 编辑 RCM 行（旧版兼容路径）。

     * <p>PUT /rcm/update/{rcm_id}，行为与 {@link #update(Long, RcmUpdateRequest)} 一致。</p>

     *

     * @param rcmId   RCM 记录 ID

     * @param request 更新字段

     * @return 更新后的 RCM 记录

     */

    @PutMapping("/update/{rcm_id}")

    public ApiResponse<RcmRecord> legacyUpdate(@PathVariable("rcm_id") Long rcmId,

                                               @Valid @RequestBody RcmUpdateRequest request) {

        return ApiResponse.success(rcmService.update(rcmId, request));

    }



    /**

     * 编辑 RCM 行（旧版兼容路径）。

     * <p>PUT /rcm/data-edit/{rcm_id}，行为与 {@link #update(Long, RcmUpdateRequest)} 一致。</p>

     *

     * @param rcmId   RCM 记录 ID

     * @param request 更新字段

     * @return 更新后的 RCM 记录

     */

    @PutMapping("/data-edit/{rcm_id}")

    public ApiResponse<RcmRecord> legacyDataEdit(@PathVariable("rcm_id") Long rcmId,

                                                 @Valid @RequestBody RcmUpdateRequest request) {

        return ApiResponse.success(rcmService.update(rcmId, request));

    }



    /**

     * 保存 RCM 版本（PRD 2.5.5 / 2.5.6）。

     * <p>POST /rcm/{rcmId}/versions，需 JWT；保存当前数据快照。</p>

     *

     * @param rcmId   RCM 记录 ID

     * @param request 版本备注/变更摘要

     * @return 新建的版本记录

     */

    @PostMapping("/{rcmId}/versions")

    public ApiResponse<RcmVersion> saveVersion(@PathVariable("rcmId") Long rcmId,

                                               @Valid @RequestBody RcmVersionCreateRequest request) {

        return ApiResponse.success(rcmService.saveVersion(rcmId, request));

    }



    /**

     * 保存 RCM 版本（旧版兼容路径）。

     * <p>POST /rcm/{rcm_id}/save-version，行为与 {@link #saveVersion(Long, RcmVersionCreateRequest)} 一致。</p>

     *

     * @param rcmId   RCM 记录 ID

     * @param request 版本备注/变更摘要

     * @return 新建的版本记录

     */

    @PostMapping("/{rcm_id}/save-version")

    public ApiResponse<RcmVersion> legacySaveVersion(@PathVariable("rcm_id") Long rcmId,

                                                     @Valid @RequestBody RcmVersionCreateRequest request) {

        return ApiResponse.success(rcmService.saveVersion(rcmId, request));

    }



    /**

     * 上传 RCM Excel 导入（PRD 2.5.5 / 2.5.6）。

     * <p>POST /rcm/upload，需 JWT；multipart/form-data，字段 file，Query projectId 必填。</p>

     *

     * @param projectId       项目 ID（camelCase）

     * @param projectIdLegacy 项目 ID（snake_case 别名）

     * @param file            Excel 文件

     * @return 导入结果（成功/失败行数）

     */

    @PostMapping("/upload")

    public ApiResponse<RcmImportResultResponse> upload(@RequestParam(value = "projectId", required = false) Long projectId,

                                                       @RequestParam(value = "project_id", required = false) Long projectIdLegacy,

                                                       @RequestParam("file") MultipartFile file) {

        return ApiResponse.success(rcmService.importExcel(projectId != null ? projectId : projectIdLegacy, file));

    }



    /**

     * 导出 RCM Excel（PRD 2.5.5）。

     * <p>GET /rcm/export，需 JWT；返回二进制流，Content-Type 为 xlsx。</p>

     *

     * @param projectId       项目 ID（camelCase）

     * @param projectIdLegacy 项目 ID（snake_case 别名）

     * @param response        HTTP 响应（写入 Excel 文件流）

     */

    @GetMapping("/export")

    public void export(@RequestParam(value = "projectId", required = false) Long projectId,

                       @RequestParam(value = "project_id", required = false) Long projectIdLegacy,

                       HttpServletResponse response) throws IOException {

        Long targetProjectId = projectId != null ? projectId : projectIdLegacy;

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        String fileName = URLEncoder.encode("RCM_Data_" + targetProjectId + ".xlsx", StandardCharsets.UTF_8);

        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + fileName);

        rcmService.exportExcel(targetProjectId, response.getOutputStream());

    }



    /**

     * 触发 AI 批量生成 RCM（PRD 2.5.7）。

     * <p>POST /rcm/ai/generate，需 JWT；基于公司/系统描述生成控制矩阵。</p>

     *

     * @param request 项目 ID、公司描述、系统描述、合规框架等

     * @return AI 生成的 RCM 记录列表

     */

    @PostMapping("/ai/generate")

    public ApiResponse<List<RcmRecord>> aiGenerate(@Valid @RequestBody RcmAiGenerateRequest request) {

        return ApiResponse.success(rcmService.generateByAi(request));

    }



    /**

     * 单行 AI 填充（PRD 2.5.6 Fill by AI）。

     * <p>POST /rcm/{rcmId}/fill-by-ai，需 JWT；对 Manual 阶段单行调用 AI 补全字段。</p>

     *

     * @param rcmId RCM 记录 ID

     * @return AI 填充后的 RCM 记录

     */

    @PostMapping("/{rcmId}/fill-by-ai")

    public ApiResponse<RcmRecord> fillByAi(@PathVariable("rcmId") Long rcmId) {

        return ApiResponse.success(rcmService.fillByAi(rcmId));

    }



    /**

     * 单行晋级到 Final（PRD 2.5.5 / 2.5.6 Upload to Final）。

     * <p>POST /rcm/{rcmId}/upload-to-final，需 JWT；将 Manual/AI 行晋级到 Final 阶段。</p>

     *

     * @param rcmId RCM 记录 ID

     * @return 晋级后的 RCM 记录

     */

    @PostMapping("/{rcmId}/upload-to-final")

    public ApiResponse<RcmRecord> uploadToFinal(@PathVariable("rcmId") Long rcmId) {

        return ApiResponse.success(rcmService.promoteToFinal(rcmId));

    }



    /**

     * 单行晋级到 Manual（PRD 2.5.7 Upload to Manual）。

     * <p>POST /rcm/{rcmId}/upload-to-manual，需 JWT；将 AI 生成行晋级到 Manual 阶段。</p>

     *

     * @param rcmId RCM 记录 ID

     * @return 晋级后的 RCM 记录

     */

    @PostMapping("/{rcmId}/upload-to-manual")

    public ApiResponse<RcmRecord> uploadToManual(@PathVariable("rcmId") Long rcmId) {

        return ApiResponse.success(rcmService.promoteToManual(rcmId));

    }



    /**

     * 单行晋级到 Manual（旧版兼容路径，拼写 mannual）。

     * <p>POST /rcm/{rcm_id}/upload-to-mannual，行为与 {@link #uploadToManual(Long)} 一致。</p>

     *

     * @param rcmId RCM 记录 ID

     * @return 晋级后的 RCM 记录

     */

    @PostMapping("/{rcm_id}/upload-to-mannual")

    public ApiResponse<RcmRecord> legacyUploadToMannual(@PathVariable("rcm_id") Long rcmId) {

        return ApiResponse.success(rcmService.promoteToManual(rcmId));

    }



    /**

     * 删除 RCM 行（PRD 2.5.5 / 2.5.6）。

     * <p>DELETE /rcm/{rcmId}，需 JWT；软删除。</p>

     *

     * @param rcmId RCM 记录 ID

     * @return 操作成功空响应

     */

    @DeleteMapping("/{rcmId}")

    public ApiResponse<Void> delete(@PathVariable("rcmId") Long rcmId) {

        rcmService.delete(rcmId);

        return ApiResponse.success();

    }



    /** 合并 snake_case 别名 Query 参数到 RcmQueryRequest。 */

    private void applyLegacyQuery(RcmQueryRequest request,

                                  Long projectIdLegacy,

                                  String categoryLegacy,

                                  String riskRatingLegacy,

                                  Long sourceRequestIdLegacy,

                                  String stageOverride) {

        if (request.getProjectId() == null) {

            request.setProjectId(projectIdLegacy);

        }

        if ((request.getCategory() == null || request.getCategory().isBlank()) && categoryLegacy != null && !categoryLegacy.isBlank()) {

            request.setCategory(categoryLegacy);

        }

        if ((request.getRiskRating() == null || request.getRiskRating().isBlank()) && riskRatingLegacy != null && !riskRatingLegacy.isBlank()) {

            request.setRiskRating(riskRatingLegacy);

        }

        if (request.getSourceRequestId() == null) {

            request.setSourceRequestId(sourceRequestIdLegacy);

        }

        if (stageOverride != null && !stageOverride.isBlank()) {

            request.setStage(stageOverride);

        }

    }

}


