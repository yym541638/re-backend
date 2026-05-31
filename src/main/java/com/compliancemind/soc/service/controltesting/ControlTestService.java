package com.compliancemind.soc.service.controltesting;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.compliancemind.soc.service.analysis.GapAnalysisService;
import com.compliancemind.soc.service.analysis.ScoreSnapshotService;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.dto.controltesting.ControlTestCreateRequest;
import com.compliancemind.soc.dto.controltesting.ControlTestDetailResponse;
import com.compliancemind.soc.dto.controltesting.ControlTestQueryRequest;
import com.compliancemind.soc.dto.controltesting.ControlTestUpdateRequest;
import com.compliancemind.soc.dto.controltesting.ControlTestVersionCreateRequest;
import com.compliancemind.soc.entity.controltesting.ControlTest;
import com.compliancemind.soc.entity.controltesting.ControlTestVersion;
import com.compliancemind.soc.mapper.controltesting.ControlTestMapper;
import com.compliancemind.soc.mapper.controltesting.ControlTestVersionMapper;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 控制测试主记录与版本、快照序列化等业务流程。
 */
@Service
public class ControlTestService {

    private final ControlTestMapper controlTestMapper;
    private final ControlTestVersionMapper controlTestVersionMapper;
    private final ProjectMapper projectMapper;
    private final UserAccountMapper userAccountMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;
    private final GapAnalysisService gapAnalysisService;
    private final ScoreSnapshotService scoreSnapshotService;
    private final ObjectMapper objectMapper;

    public ControlTestService(ControlTestMapper controlTestMapper,
                              ControlTestVersionMapper controlTestVersionMapper,
                              ProjectMapper projectMapper,
                              UserAccountMapper userAccountMapper,
                              AuthorizationService authorizationService,
                              CurrentUserAccessor currentUserAccessor,
                              OperationLogService operationLogService,
                              GapAnalysisService gapAnalysisService,
                              ScoreSnapshotService scoreSnapshotService,
                              ObjectMapper objectMapper) {
        this.controlTestMapper = controlTestMapper;
        this.controlTestVersionMapper = controlTestVersionMapper;
        this.projectMapper = projectMapper;
        this.userAccountMapper = userAccountMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
        this.gapAnalysisService = gapAnalysisService;
        this.scoreSnapshotService = scoreSnapshotService;
        this.objectMapper = objectMapper;
    }

    public List<ControlTest> list(ControlTestQueryRequest request) {
        if (request.getProjectId() == null) {
            throw new BizException(BizErrorCode.PROJECT_ID_REQUIRED);
        }
        authorizationService.requireProjectRead(request.getProjectId());
        return controlTestMapper.list(request);
    }

    public ControlTestDetailResponse detail(Long testId) {
        ControlTest controlTest = requireOwnedTest(testId);
        ControlTestDetailResponse response = new ControlTestDetailResponse();
        response.setControlTest(controlTest);
        response.setVersions(controlTestVersionMapper.listByTestId(testId));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public ControlTest create(ControlTestCreateRequest request) {
        Project project = authorizationService.requireProjectWrite(request.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        ControlTest controlTest = new ControlTest();
        controlTest.setProjectId(project.getProjectId());
        controlTest.setTitle(request.getTitle().trim());
        controlTest.setDescription(request.getDescription());
        controlTest.setRiskLevel(defaultText(request.getRiskLevel(), SocConstants.ControlTest.RISK_MEDIUM));
        controlTest.setRiskDescription(request.getRiskDescription());
        controlTest.setCosoPrinciple(request.getCosoPrinciple());
        controlTest.setControlProcedure(request.getControlProcedure());
        controlTest.setResultStatus(defaultText(request.getResultStatus(), SocConstants.ControlTest.RESULT_PENDING));
        controlTest.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
        controlTest.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        controlTest.setCreatedBy(operatorId);
        controlTest.setUpdatedBy(operatorId);
        controlTestMapper.insert(controlTest);
        saveSnapshot(controlTest, SocConstants.OperationLog.Detail.RCM_SNAPSHOT_INITIAL_VERSION_EN);
        syncProjectOutputs(controlTest.getProjectId());
        operationLogService.record(SocConstants.OperationLog.Module.CONTROL_TEST,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.CONTROL_TEST,
            String.valueOf(controlTest.getTestId()),
            controlTest.getTitle(),
            controlTest.getProjectId(),
            SocConstants.OperationLog.Detail.CONTROL_TEST_CREATE_EN);
        return controlTest;
    }

    @Transactional(rollbackFor = Exception.class)
    public ControlTest update(Long testId, ControlTestUpdateRequest request) {
        ControlTest controlTest = requireOwnedTest(testId);
        authorizationService.requireProjectWrite(controlTest.getProjectId());
        controlTest.setTitle(request.getTitle().trim());
        controlTest.setDescription(request.getDescription());
        controlTest.setRiskLevel(defaultText(request.getRiskLevel(), controlTest.getRiskLevel()));
        controlTest.setRiskDescription(request.getRiskDescription());
        controlTest.setCosoPrinciple(request.getCosoPrinciple());
        controlTest.setControlProcedure(request.getControlProcedure());
        controlTest.setResultStatus(defaultText(request.getResultStatus(), controlTest.getResultStatus()));
        controlTest.setCurrentVersion(nextVersion(controlTest.getCurrentVersion()));
        controlTest.setUpdatedBy(currentUserAccessor.requireUserId());
        controlTestMapper.update(controlTest);
        saveSnapshot(controlTest, defaultText(request.getChangeSummary(), SocConstants.OperationLog.Detail.CONTROL_TEST_UPDATE_EN));
        syncProjectOutputs(controlTest.getProjectId());
        operationLogService.record(SocConstants.OperationLog.Module.CONTROL_TEST,
            SocConstants.OperationLog.Action.UPDATE,
            SocConstants.OperationLog.EntityType.CONTROL_TEST,
            String.valueOf(controlTest.getTestId()),
            controlTest.getTitle(),
            controlTest.getProjectId(),
            defaultText(request.getChangeSummary(), SocConstants.OperationLog.Detail.CONTROL_TEST_UPDATE_EN));
        return controlTest;
    }

    @Transactional(rollbackFor = Exception.class)
    public ControlTestVersion saveVersion(Long testId, ControlTestVersionCreateRequest request) {
        ControlTest controlTest = requireOwnedTest(testId);
        authorizationService.requireProjectWrite(controlTest.getProjectId());
        saveSnapshot(controlTest, request.getChangeSummary());
        operationLogService.record(SocConstants.OperationLog.Module.CONTROL_TEST,
            SocConstants.OperationLog.Action.SAVE_VERSION,
            SocConstants.OperationLog.EntityType.CONTROL_TEST,
            String.valueOf(testId),
            controlTest.getTitle(),
            controlTest.getProjectId(),
            request.getChangeSummary());
        return controlTestVersionMapper.listByTestId(testId).stream().findFirst().orElseThrow(() -> new BizException(BizErrorCode.CONTROL_TEST_SAVE_VERSION_FAILED));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long testId) {
        ControlTest controlTest = requireOwnedTest(testId);
        authorizationService.requireProjectWrite(controlTest.getProjectId());
        controlTestMapper.softDelete(testId, currentUserAccessor.requireUserId());
        syncProjectOutputs(controlTest.getProjectId());
        operationLogService.record(SocConstants.OperationLog.Module.CONTROL_TEST,
            SocConstants.OperationLog.Action.DELETE,
            SocConstants.OperationLog.EntityType.CONTROL_TEST,
            String.valueOf(testId),
            controlTest.getTitle(),
            controlTest.getProjectId(),
            SocConstants.OperationLog.Detail.CONTROL_TEST_DELETE_EN);
    }

    private void syncProjectOutputs(Long projectId) {
        gapAnalysisService.regenerate(projectId);
        scoreSnapshotService.recordSnapshot(projectId);
    }

    private void saveSnapshot(ControlTest controlTest, String changeSummary) {
        ControlTestVersion version = new ControlTestVersion();
        version.setTestId(controlTest.getTestId());
        version.setVersionNo(controlTest.getCurrentVersion());
        version.setSnapshotJson(writeJson(controlTest));
        version.setChangeSummary(changeSummary);
        version.setCreatedBy(currentUserAccessor.requireUserId());
        controlTestVersionMapper.insert(version);
    }

    private ControlTest requireOwnedTest(Long testId) {
        ControlTest controlTest = controlTestMapper.selectById(testId);
        if (controlTest == null) {
            throw new BizException(BizErrorCode.CONTROL_TEST_NOT_FOUND);
        }
        authorizationService.requireProjectRead(controlTest.getProjectId());
        return controlTest;
    }

    private void validateProjectOwnership(Long projectId) {
        if (projectId != null) {
            authorizationService.requireProjectRead(projectId);
        }
    }

    private String nextVersion(String currentVersion) {
        if (currentVersion == null || currentVersion.isBlank()) {
            return SocConstants.Project.INITIAL_VERSION;
        }
        try {
            int number = Integer.parseInt(currentVersion.replace(SocConstants.Rcm.VERSION_PREFIX, ""));
            return SocConstants.Rcm.VERSION_PREFIX + (number + 1);
        } catch (NumberFormatException exception) {
            return SocConstants.Project.INITIAL_VERSION;
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException(BizErrorCode.CONTROL_TEST_SNAPSHOT_GENERATION_FAILED);
        }
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
