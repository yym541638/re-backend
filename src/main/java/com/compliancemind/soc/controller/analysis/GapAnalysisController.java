package com.compliancemind.soc.controller.analysis;



import com.compliancemind.soc.dto.analysis.GapAnalysisQueryRequest;

import com.compliancemind.soc.entity.analysis.GapAnalysisRecord;

import com.compliancemind.soc.service.analysis.GapAnalysisService;

import com.compliancemind.soc.common.api.ApiResponse;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;



import java.util.List;



/**

 * 差距分析记录列表（按项目等作用域查询）。

 * <p>对应 PRD 2.5.9 Gap Analysis；页面只读展示，后端支持手动重算。</p>

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

     * <p>GET /gap-analysis/list，需 JWT；按 projectId、gapLevel、status 筛选。</p>

     *

     * @param request 查询条件

     * @return 差距分析条目列表

     */

    @GetMapping("/list")

    public ApiResponse<List<GapAnalysisRecord>> list(GapAnalysisQueryRequest request) {

        return ApiResponse.success(gapAnalysisService.list(request));

    }



    /**

     * 重新生成差距分析（扩展）。

     * <p>POST /gap-analysis/regenerate?projectId=xxx，需 JWT；基于控制测试结果重新计算差距。</p>

     *

     * @param projectId 项目 ID（必填）

     * @return 重新生成后的差距分析列表

     */

    @PostMapping("/regenerate")

    public ApiResponse<List<GapAnalysisRecord>> regenerate(@RequestParam("projectId") Long projectId) {

        return ApiResponse.success(gapAnalysisService.regenerate(projectId));

    }

}


