package com.compliancemind.soc.service.analysis;

import com.compliancemind.soc.dto.analysis.GapAnalysisQueryRequest;
import com.compliancemind.soc.entity.analysis.GapAnalysisRecord;
import com.compliancemind.soc.mapper.analysis.GapAnalysisMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.entity.controltesting.ControlTest;
import com.compliancemind.soc.mapper.controltesting.ControlTestMapper;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.compliancemind.soc.security.AuthorizationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 差距分析记录生成与列表（常与控制测试结果联动）。
 */
@Service
public class GapAnalysisService {

    private final GapAnalysisMapper gapAnalysisMapper;
    private final ControlTestMapper controlTestMapper;
    private final OperationLogService operationLogService;
    private final AuthorizationService authorizationService;

    public GapAnalysisService(GapAnalysisMapper gapAnalysisMapper,
                              ControlTestMapper controlTestMapper,
                              OperationLogService operationLogService,
                              AuthorizationService authorizationService) {
        this.gapAnalysisMapper = gapAnalysisMapper;
        this.controlTestMapper = controlTestMapper;
        this.operationLogService = operationLogService;
        this.authorizationService = authorizationService;
    }

    public List<GapAnalysisRecord> list(GapAnalysisQueryRequest request) {
        if (request.getProjectId() == null) {
            throw new BizException(BizErrorCode.GAP_ANALYSIS_PROJECT_ID_REQUIRED);
        }
        authorizationService.requireProjectRead(request.getProjectId());
        return gapAnalysisMapper.list(request);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<GapAnalysisRecord> regenerate(Long projectId) {
        authorizationService.requireProjectWrite(projectId);
        gapAnalysisMapper.deleteByProjectId(projectId);
        List<ControlTest> controlTests = controlTestMapper.listByProjectId(projectId);
        List<GapAnalysisRecord> results = new ArrayList<>();
        for (ControlTest controlTest : controlTests) {
            if (SocConstants.ControlTest.RESULT_PASS.equalsIgnoreCase(controlTest.getResultStatus())) {
                continue;
            }
            GapAnalysisRecord record = new GapAnalysisRecord();
            record.setProjectId(projectId);
            record.setSourceTestId(controlTest.getTestId());
            record.setControlTitle(controlTest.getTitle());
            record.setGapLevel(resolveGapLevel(controlTest.getRiskLevel(), controlTest.getResultStatus()));
            record.setStatus(SocConstants.GapAnalysis.STATUS_OPEN);
            record.setGapDescription(buildGapDescription(controlTest));
            record.setRemediationSuggestion(buildSuggestion(controlTest));
            gapAnalysisMapper.insert(record);
            results.add(record);
        }
        operationLogService.record(SocConstants.OperationLog.Module.GAP_ANALYSIS,
            SocConstants.OperationLog.Action.REGENERATE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(projectId),
            SocConstants.GapAnalysis.TITLE_DEFAULT,
            projectId,
            SocConstants.OperationLog.Detail.GAP_REGENERATE_ZH);
        return results;
    }

    public long countByProjectId(Long projectId) {
        return gapAnalysisMapper.countByProjectId(projectId);
    }

    private String resolveGapLevel(String riskLevel, String resultStatus) {
        if (SocConstants.ControlTest.RESULT_FAIL.equalsIgnoreCase(resultStatus)) {
            return riskLevel == null || riskLevel.isBlank()
                ? SocConstants.GapAnalysis.RISK_HIGH
                : riskLevel.toUpperCase();
        }
        return SocConstants.GapAnalysis.RISK_MEDIUM;
    }

    private String buildGapDescription(ControlTest controlTest) {
        return SocConstants.GapAnalysis.DESCRIPTION_PREFIX_ZH + defaultText(controlTest.getTitle());
    }

    private String buildSuggestion(ControlTest controlTest) {
        return SocConstants.GapAnalysis.SUGGESTION_PREFIX_ZH + defaultText(controlTest.getControlProcedure());
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? SocConstants.GapAnalysis.DEFAULT_CONTROL_TITLE_ZH : value;
    }
}
