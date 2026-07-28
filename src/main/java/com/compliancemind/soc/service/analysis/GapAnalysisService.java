package com.compliancemind.soc.service.analysis;

import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.dto.analysis.GapAnalysisQueryRequest;
import com.compliancemind.soc.dto.analysis.GapAnalysisSaveRequest;
import com.compliancemind.soc.entity.analysis.GapAnalysisRecord;
import com.compliancemind.soc.entity.controltesting.ControlTest;
import com.compliancemind.soc.mapper.analysis.GapAnalysisMapper;
import com.compliancemind.soc.mapper.controltesting.ControlTestMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.service.operationlog.OperationLogService;
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
    public GapAnalysisRecord create(Long projectId, GapAnalysisSaveRequest request) {
        if (projectId == null) {
            throw new BizException(BizErrorCode.GAP_ANALYSIS_PROJECT_ID_REQUIRED);
        }
        authorizationService.requireProjectWrite(projectId);
        GapAnalysisRecord record = new GapAnalysisRecord();
        record.setProjectId(projectId);
        applySaveRequest(record, request);
        gapAnalysisMapper.insert(record);
        operationLogService.record(SocConstants.OperationLog.Module.GAP_ANALYSIS,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(record.getGapId()),
            record.getControlTitle(),
            projectId,
            "Create gap analysis");
        return gapAnalysisMapper.selectById(record.getGapId());
    }

    @Transactional(rollbackFor = Exception.class)
    public GapAnalysisRecord update(Long gapId, GapAnalysisSaveRequest request) {
        GapAnalysisRecord existing = gapAnalysisMapper.selectById(gapId);
        if (existing == null) {
            throw new BizException(BizErrorCode.GAP_ANALYSIS_NOT_FOUND);
        }
        authorizationService.requireProjectWrite(existing.getProjectId());
        applySaveRequest(existing, request);
        gapAnalysisMapper.update(existing);
        operationLogService.record(SocConstants.OperationLog.Module.GAP_ANALYSIS,
            SocConstants.OperationLog.Action.UPDATE,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(gapId),
            existing.getControlTitle(),
            existing.getProjectId(),
            "Update gap analysis");
        return gapAnalysisMapper.selectById(gapId);
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

    private void applySaveRequest(GapAnalysisRecord record, GapAnalysisSaveRequest request) {
        if (request == null) {
            throw new BizException(BizErrorCode.GAP_ANALYSIS_TITLE_REQUIRED);
        }
        String title = request.getControlTitle();
        if (title == null || title.isBlank()) {
            throw new BizException(BizErrorCode.GAP_ANALYSIS_TITLE_REQUIRED);
        }
        record.setControlTitle(title.trim());
        record.setSourceTestId(request.getSourceTestId());
        record.setGapDescription(request.getGapDescription());
        record.setRemediationSuggestion(request.getRemediationSuggestion());
        record.setGapLevel(request.getGapLevel() == null || request.getGapLevel().isBlank()
            ? SocConstants.GapAnalysis.RISK_MEDIUM
            : request.getGapLevel().trim().toUpperCase());
        record.setStatus(resolveStatus(request.getRemediationYesNo()));
    }

    private String resolveStatus(String remediationYesNo) {
        if (remediationYesNo == null || remediationYesNo.isBlank()) {
            return SocConstants.GapAnalysis.STATUS_OPEN;
        }
        String normalized = remediationYesNo.trim().toUpperCase();
        if ("YES".equals(normalized) || "Y".equals(normalized) || "TRUE".equals(normalized)
            || SocConstants.GapAnalysis.STATUS_CLOSED.equals(normalized)) {
            return SocConstants.GapAnalysis.STATUS_CLOSED;
        }
        return SocConstants.GapAnalysis.STATUS_OPEN;
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
