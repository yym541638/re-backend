package com.compliancemind.soc.controller.analysis;


import com.compliancemind.soc.dto.analysis.PassRateResponse;

import com.compliancemind.soc.dto.analysis.GenerateReportRequest;

import com.compliancemind.soc.dto.analysis.ReportTaskResponse;

import com.compliancemind.soc.dto.analysis.TrendPoint;

import com.compliancemind.soc.service.analysis.AnalysisService;

import com.compliancemind.soc.service.analysis.ReportService;

import com.compliancemind.soc.common.api.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;


import java.util.List;


/**
 * 分析与报告：通过率、趋势、生成报告任务及下载。
 *
 * <p>对应 PRD 2.5.10 Passing Scores；报告导出为扩展能力。</p>
 */

@RestController

@RequestMapping("/analysis")

public class AnalysisController {


    private final AnalysisService analysisService;

    private final ReportService reportService;


    public AnalysisController(AnalysisService analysisService, ReportService reportService) {

        this.analysisService = analysisService;

        this.reportService = reportService;

    }


    /**
     * 通过率（Passing Scores）（PRD 2.5.10）。
     *
     * <p>GET /analysis/pass-rate/{projectId}，需 JWT；只读展示项目合规通过率。</p>
     *
     * @param projectId 项目 ID
     * @return 通过率及各项计数
     */

    @GetMapping("/pass-rate/{projectId}")

    public ApiResponse<PassRateResponse> passRate(@PathVariable("projectId") Long projectId) {

        return ApiResponse.success(analysisService.getPassRate(projectId));

    }


    /**
     * 分数趋势（PRD 2.5.10）。
     *
     * <p>GET /analysis/trend/{projectId}，需 JWT；按日快照展示通过率变化趋势。</p>
     *
     * @param projectId 项目 ID
     * @return 趋势数据点列表
     */

    @GetMapping("/trend/{projectId}")

    public ApiResponse<List<TrendPoint>> trend(@PathVariable("projectId") Long projectId) {

        return ApiResponse.success(analysisService.getTrend(projectId));

    }


    /**
     * 分数趋势（旧版兼容路径）。
     *
     * <p>GET /analysis/trend?project_id=xxx，行为与 {@link #trend(Long)} 一致。</p>
     *
     * @param projectId 项目 ID
     * @return 趋势数据点列表
     */

    @GetMapping("/trend")

    public ApiResponse<List<TrendPoint>> legacyTrend(@RequestParam(value = "project_id") Long projectId) {

        return ApiResponse.success(analysisService.getTrend(projectId));

    }


    /**
     * 通过率详情（旧版兼容路径）。
     *
     * <p>GET /analysis/pass-rate/detail/{project_id}，行为与 {@link #passRate(Long)} 一致。</p>
     *
     * @param projectId 项目 ID
     * @return 通过率及各项计数
     */

    @GetMapping("/pass-rate/detail/{project_id}")

    public ApiResponse<PassRateResponse> legacyPassRateDetail(@PathVariable("project_id") Long projectId) {

        return ApiResponse.success(analysisService.getPassRate(projectId));

    }


    /**
     * 创建报告生成任务（扩展）。
     *
     * <p>POST /analysis/generate-report，需 JWT；异步生成合规分析报告。</p>
     *
     * @param request 项目 ID、报告类型、格式、章节及语言
     * @return 报告任务信息（含 taskId）
     */

    @PostMapping("/generate-report")

    public ApiResponse<ReportTaskResponse> generateReport(@Valid @RequestBody GenerateReportRequest request) {

        return ApiResponse.success(reportService.createTask(request));

    }


    /**
     * 查询报告任务状态（扩展）。
     *
     * <p>GET /analysis/report-status/{taskId}，需 JWT；轮询报告生成进度。</p>
     *
     * @param taskId 报告任务 ID
     * @return 任务状态及进度
     */

    @GetMapping("/report-status/{taskId}")

    public ApiResponse<ReportTaskResponse> reportStatus(@PathVariable("taskId") Long taskId) {

        return ApiResponse.success(reportService.status(taskId));

    }


    /**
     * 下载报告文件（扩展）。
     *
     * <p>GET /analysis/download-report/{taskId}，需 JWT；返回二进制文件流。</p>
     *
     * @param taskId   报告任务 ID
     * @param response HTTP 响应（写入文件流）
     */

    @GetMapping("/download-report/{taskId}")

    public void downloadReport(@PathVariable("taskId") Long taskId, HttpServletResponse response) {

        reportService.download(taskId, response);

    }

}


