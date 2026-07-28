package com.compliancemind.soc.controller.analysis;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.dto.analysis.GapAnalysisQueryRequest;
import com.compliancemind.soc.dto.analysis.GapAnalysisSaveRequest;
import com.compliancemind.soc.entity.analysis.GapAnalysisRecord;
import com.compliancemind.soc.service.analysis.GapAnalysisService;
import jakarta.validation.Valid;
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
 * 差距分析记录列表（按项目等作用域查询）。
 *
 * <p>对应 PRD 2.5.9 Gap Analysis。</p>
 */
@RestController
@RequestMapping("/gap-analysis")
public class GapAnalysisController {

    private final GapAnalysisService gapAnalysisService;

    public GapAnalysisController(GapAnalysisService gapAnalysisService) {
        this.gapAnalysisService = gapAnalysisService;
    }

    /**
     * 差距分析列表（PRD 2.5.9）。
     *
     * <p>GET /gap-analysis/list，需 JWT；按 projectId、gapLevel、status 筛选。</p>
     */
    @GetMapping("/list")
    public ApiResponse<List<GapAnalysisRecord>> list(GapAnalysisQueryRequest request) {
        return ApiResponse.success(gapAnalysisService.list(request));
    }

    /**
     * 新建差距分析条目（Gap Analysis「New」）。
     *
     * <p>POST /gap-analysis?projectId=xxx，需 JWT。</p>
     */
    @PostMapping
    public ApiResponse<GapAnalysisRecord> create(@RequestParam("projectId") Long projectId,
                                                 @Valid @RequestBody GapAnalysisSaveRequest request) {
        return ApiResponse.success(gapAnalysisService.create(projectId, request));
    }

    /**
     * 更新差距分析条目。
     *
     * <p>PUT /gap-analysis/{gapId}，需 JWT。</p>
     */
    @PutMapping("/{gapId}")
    public ApiResponse<GapAnalysisRecord> update(@PathVariable("gapId") Long gapId,
                                                 @Valid @RequestBody GapAnalysisSaveRequest request) {
        return ApiResponse.success(gapAnalysisService.update(gapId, request));
    }

    /**
     * 重新生成差距分析（扩展）。
     *
     * <p>POST /gap-analysis/regenerate?projectId=xxx，需 JWT；基于控制测试结果重新计算差距。</p>
     */
    @PostMapping("/regenerate")
    public ApiResponse<List<GapAnalysisRecord>> regenerate(@RequestParam("projectId") Long projectId) {
        return ApiResponse.success(gapAnalysisService.regenerate(projectId));
    }
}
