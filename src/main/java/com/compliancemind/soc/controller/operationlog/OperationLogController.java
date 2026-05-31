package com.compliancemind.soc.controller.operationlog;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.dto.operationlog.OperationLogQueryRequest;

import com.compliancemind.soc.dto.operationlog.OperationLogStatisticsResponse;

import com.compliancemind.soc.entity.operationlog.OperationLog;

import com.compliancemind.soc.service.operationlog.OperationLogService;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;



import java.util.List;



/**

 * 操作日志：列表与统计。

 * <p>对应 PRD 2.5.11 Operation Log。</p>

 */

@RestController

@RequestMapping("/operation-log")

public class OperationLogController {



    private final OperationLogService operationLogService;



    public OperationLogController(OperationLogService operationLogService) {

        this.operationLogService = operationLogService;

    }



    /**

     * 操作日志列表（PRD 2.5.11）。

     * <p>GET /operation-log/list，需 JWT；projectId 必填，可按 moduleName、actionType 筛选。</p>

     *

     * @param request 查询条件（projectId、moduleName、actionType）

     * @return 操作日志列表

     */

    @GetMapping("/list")

    public ApiResponse<List<OperationLog>> list(OperationLogQueryRequest request) {

        return ApiResponse.success(operationLogService.list(request));

    }



    /**

     * 操作日志统计（扩展）。

     * <p>GET /operation-log/statistics?projectId=xxx，需 JWT；按模块/动作类型汇总统计。</p>

     *

     * @param projectId 项目 ID（必填）

     * @return 各模块操作次数统计

     */

    @GetMapping("/statistics")

    public ApiResponse<OperationLogStatisticsResponse> statistics(@RequestParam("projectId") Long projectId) {

        return ApiResponse.success(operationLogService.statistics(projectId));

    }

}


