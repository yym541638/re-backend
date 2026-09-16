package com.compliancemind.soc.controller.risk;

import com.compliancemind.soc.common.api.ApiResponse;
import com.compliancemind.soc.common.api.PageResponse;
import com.compliancemind.soc.dto.risk.RiskCreateRequest;
import com.compliancemind.soc.dto.risk.RiskQueryRequest;
import com.compliancemind.soc.dto.risk.RiskUpdateRequest;
import com.compliancemind.soc.entity.risk.RiskRecord;
import com.compliancemind.soc.service.risk.RiskService;
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

import java.util.List;

/**
 * Risk table：列表、详情、新建、编辑、删除。
 */
@RestController
@RequestMapping("/risk-table")
public class RiskController {

    private final RiskService riskService;

    public RiskController(RiskService riskService) {
        this.riskService = riskService;
    }

    /**
     * 风险列表（分页）。
     *
     * <p>GET /risk-table/list?projectId=&amp;ccCriteria=&amp;riskLevel=&amp;keyword=&amp;pageNum=&amp;pageSize=</p>
     */
    @GetMapping("/list")
    public ApiResponse<PageResponse<RiskRecord>> list(
            @RequestParam("projectId") Long projectId,
            @RequestParam(value = "ccCriteria", required = false) String ccCriteria,
            @RequestParam(value = "cc_criteria", required = false) String ccCriteriaLegacy,
            @RequestParam(value = "riskLevel", required = false) String riskLevel,
            @RequestParam(value = "risk_level", required = false) String riskLevelLegacy,
            @RequestParam(value = "riskSource", required = false) String riskSource,
            @RequestParam(value = "risk_source", required = false) String riskSourceLegacy,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        RiskQueryRequest request = new RiskQueryRequest();
        request.setProjectId(projectId);
        request.setCcCriteria(ccCriteria != null ? ccCriteria : ccCriteriaLegacy);
        request.setRiskLevel(riskLevel != null ? riskLevel : riskLevelLegacy);
        request.setRiskSource(riskSource != null ? riskSource : riskSourceLegacy);
        request.setKeyword(keyword);
        request.setPageNum(pageNum);
        request.setPageSize(pageSize);
        return ApiResponse.page(riskService.list(request));
    }

    /**
     * 按已购套餐范围，从标准条款目录初始化 Risk table。
     *
     * <p>POST /risk-table/generate?projectId=</p>
     */
    @PostMapping("/generate")
    public ApiResponse<List<RiskRecord>> generate(@RequestParam("projectId") Long projectId) {
        return ApiResponse.success(riskService.generateFromCatalog(projectId));
    }

    /**
     * 风险详情。
     *
     * <p>GET /risk-table/{riskId}</p>
     */
    @GetMapping("/{riskId}")
    public ApiResponse<RiskRecord> detail(@PathVariable("riskId") Long riskId) {
        return ApiResponse.success(riskService.detail(riskId));
    }

    /**
     * 新建风险（页面 New）。
     *
     * <p>POST /risk-table</p>
     */
    @PostMapping
    public ApiResponse<RiskRecord> create(@Valid @RequestBody RiskCreateRequest request) {
        return ApiResponse.success(riskService.create(request));
    }

    /**
     * 编辑风险（Operation · Edit）。
     *
     * <p>PUT /risk-table/{riskId}</p>
     */
    @PutMapping("/{riskId}")
    public ApiResponse<RiskRecord> update(@PathVariable("riskId") Long riskId,
                                          @Valid @RequestBody RiskUpdateRequest request) {
        return ApiResponse.success(riskService.update(riskId, request));
    }

    /**
     * 删除风险（Operation · Delete，软删除）。
     *
     * <p>DELETE /risk-table/{riskId}</p>
     */
    @DeleteMapping("/{riskId}")
    public ApiResponse<Void> delete(@PathVariable("riskId") Long riskId) {
        riskService.delete(riskId);
        return ApiResponse.success();
    }
}
