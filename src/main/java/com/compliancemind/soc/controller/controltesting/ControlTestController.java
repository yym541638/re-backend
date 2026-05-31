package com.compliancemind.soc.controller.controltesting;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.dto.controltesting.ControlTestCreateRequest;

import com.compliancemind.soc.dto.controltesting.ControlTestDetailResponse;

import com.compliancemind.soc.dto.controltesting.ControlTestQueryRequest;

import com.compliancemind.soc.dto.controltesting.ControlTestUpdateRequest;

import com.compliancemind.soc.dto.controltesting.ControlTestVersionCreateRequest;

import com.compliancemind.soc.entity.controltesting.ControlTest;

import com.compliancemind.soc.entity.controltesting.ControlTestVersion;

import com.compliancemind.soc.service.controltesting.ControlTestService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;



import java.util.List;



/**

 * 控制测试：列表、详情、CRUD、版本快照。

 * <p>对应 PRD 2.5.8 Control Testing。</p>

 */

@RestController

@RequestMapping("/control-testing")

public class ControlTestController {



    private final ControlTestService controlTestService;



    public ControlTestController(ControlTestService controlTestService) {

        this.controlTestService = controlTestService;

    }



    /**

     * 控制测试列表（PRD 2.5.8）。

     * <p>GET /control-testing/list，需 JWT；projectId 必填，可按 resultStatus、riskLevel 筛选。</p>

     *

     * @param request 查询条件

     * @return 控制测试列表

     */

    @GetMapping("/list")

    public ApiResponse<List<ControlTest>> list(ControlTestQueryRequest request) {

        return ApiResponse.success(controlTestService.list(request));

    }



    /**

     * 控制测试详情（PRD 2.5.8）。

     * <p>GET /control-testing/{testId}，需 JWT。</p>

     *

     * @param testId 控制测试 ID

     * @return 控制测试详情及版本列表

     */

    @GetMapping("/{testId}")

    public ApiResponse<ControlTestDetailResponse> detail(@PathVariable("testId") Long testId) {

        return ApiResponse.success(controlTestService.detail(testId));

    }



    /**

     * 新建控制测试（PRD 2.5.8）。

     * <p>POST /control-testing，需 JWT；可由 RCM 自动生成后编辑。</p>

     *

     * @param request 控制测试创建字段

     * @return 新建的控制测试

     */

    @PostMapping

    public ApiResponse<ControlTest> create(@Valid @RequestBody ControlTestCreateRequest request) {

        return ApiResponse.success(controlTestService.create(request));

    }



    /**

     * 编辑控制测试（PRD 2.5.8）。

     * <p>PUT /control-testing/{testId}，需 JWT。</p>

     *

     * @param testId  控制测试 ID

     * @param request 更新字段

     * @return 更新后的控制测试

     */

    @PutMapping("/{testId}")

    public ApiResponse<ControlTest> update(@PathVariable("testId") Long testId,

                                           @Valid @RequestBody ControlTestUpdateRequest request) {

        return ApiResponse.success(controlTestService.update(testId, request));

    }



    /**

     * 保存控制测试版本（PRD 2.5.8）。

     * <p>POST /control-testing/{testId}/versions，需 JWT；保存当前数据快照。</p>

     *

     * @param testId  控制测试 ID

     * @param request 版本备注/变更摘要

     * @return 新建的版本记录

     */

    @PostMapping("/{testId}/versions")

    public ApiResponse<ControlTestVersion> saveVersion(@PathVariable("testId") Long testId,

                                                       @Valid @RequestBody ControlTestVersionCreateRequest request) {

        return ApiResponse.success(controlTestService.saveVersion(testId, request));

    }



    /**

     * 删除控制测试（PRD 2.5.8）。

     * <p>DELETE /control-testing/{testId}，需 JWT；软删除。</p>

     *

     * @param testId 控制测试 ID

     * @return 操作成功空响应

     */

    @DeleteMapping("/{testId}")

    public ApiResponse<Void> delete(@PathVariable("testId") Long testId) {

        controlTestService.delete(testId);

        return ApiResponse.success();

    }

}


