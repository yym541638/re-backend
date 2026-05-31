package com.compliancemind.soc.service.rcm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.dto.rcm.RcmAiGenerateRequest;
import com.compliancemind.soc.dto.rcm.RcmCreateRequest;
import com.compliancemind.soc.dto.rcm.RcmDetailResponse;
import com.compliancemind.soc.dto.rcm.RcmImportResultResponse;
import com.compliancemind.soc.dto.rcm.RcmQueryRequest;
import com.compliancemind.soc.dto.rcm.RcmUpdateRequest;
import com.compliancemind.soc.dto.rcm.RcmVersionCreateRequest;
import com.compliancemind.soc.entity.rcm.RcmRecord;
import com.compliancemind.soc.entity.rcm.RcmVersion;
import com.compliancemind.soc.mapper.rcm.RcmRecordMapper;
import com.compliancemind.soc.mapper.rcm.RcmVersionMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * RCM 核心业务：列表筛选、CRUD、阶段晋级、AI/Excel、与 Request 同步等。
 */
@Service
public class RcmService {

    private final RcmRecordMapper rcmRecordMapper;
    private final RcmVersionMapper rcmVersionMapper;
    private final ProjectMapper projectMapper;
    private final UserAccountMapper userAccountMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final ObjectMapper objectMapper;
    private final OperationLogService operationLogService;
    private final RcmExcelService rcmExcelService;
    private final RcmAiService rcmAiService;

    public RcmService(RcmRecordMapper rcmRecordMapper,
                      RcmVersionMapper rcmVersionMapper,
                      ProjectMapper projectMapper,
                      UserAccountMapper userAccountMapper,
                      AuthorizationService authorizationService,
                      CurrentUserAccessor currentUserAccessor,
                      ObjectMapper objectMapper,
                      OperationLogService operationLogService,
                      RcmExcelService rcmExcelService,
                      RcmAiService rcmAiService) {
        this.rcmRecordMapper = rcmRecordMapper;
        this.rcmVersionMapper = rcmVersionMapper;
        this.projectMapper = projectMapper;
        this.userAccountMapper = userAccountMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.objectMapper = objectMapper;
        this.operationLogService = operationLogService;
        this.rcmExcelService = rcmExcelService;
        this.rcmAiService = rcmAiService;
    }

    public List<RcmRecord> list(RcmQueryRequest request) {
        if (request.getProjectId() == null) {
            throw new BizException(BizErrorCode.PROJECT_ID_REQUIRED);
        }
        authorizationService.requireProjectRead(request.getProjectId());
        return rcmRecordMapper.list(request);
    }

    public RcmDetailResponse detail(Long rcmId) {
        RcmRecord rcmRecord = requireOwnedRcm(rcmId);
        RcmDetailResponse response = new RcmDetailResponse();
        response.setRcm(rcmRecord);
        response.setVersions(rcmVersionMapper.listByRcmId(rcmId));
        return response;
    }

    public Map<String, Object> aiStatus() {
        return Map.of(
            "available", rcmAiService.checkServiceAvailable(),
            "modelName", rcmAiService.modelName()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public RcmRecord create(RcmCreateRequest request) {
        Project project = authorizationService.requireProjectWrite(request.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        RcmRecord rcmRecord = new RcmRecord();
        rcmRecord.setProjectId(project.getProjectId());
        rcmRecord.setControlCode(request.getControlCode().trim());
        rcmRecord.setControlName(request.getControlName().trim());
        rcmRecord.setDescription(request.getDescription());
        rcmRecord.setCategory(request.getCategory());
        rcmRecord.setModuleName(request.getModuleName());
        rcmRecord.setRiskDescription(request.getRiskDescription());
        rcmRecord.setStatus(defaultText(request.getStatus(), SocConstants.Rcm.STATUS_PENDING));
        rcmRecord.setStage(normalizeStage(request.getStage(), SocConstants.Rcm.STAGE_MANUAL));
        rcmRecord.setAiGenerated(Boolean.TRUE.equals(request.getAiGenerated()));
        rcmRecord.setSourceRequestId(request.getSourceRequestId());
        rcmRecord.setSourceRcmId(null);
        rcmRecord.setControlObjective(request.getControlObjective());
        rcmRecord.setImplementationMethod(request.getImplementationMethod());
        rcmRecord.setEvidenceRequirement(request.getEvidenceRequirement());
        rcmRecord.setControlPerformer(request.getControlPerformer());
        rcmRecord.setControlReviewer(request.getControlReviewer());
        rcmRecord.setAdditionalOwner(request.getAdditionalOwner());
        rcmRecord.setControlRiskRating(request.getControlRiskRating());
        rcmRecord.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
        rcmRecord.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        rcmRecord.setCreatedBy(operatorId);
        rcmRecord.setUpdatedBy(operatorId);
        rcmRecordMapper.insert(rcmRecord);
        saveSnapshot(rcmRecord, "Initial version");
        operationLogService.record("RCM", "CREATE", "RCM", String.valueOf(rcmRecord.getRcmId()), rcmRecord.getControlName(), rcmRecord.getProjectId(), "Create RCM");
        return rcmRecord;
    }

    @Transactional(rollbackFor = Exception.class)
    public RcmRecord update(Long rcmId, RcmUpdateRequest request) {
        RcmRecord rcmRecord = requireOwnedRcm(rcmId);
        authorizationService.requireProjectWrite(rcmRecord.getProjectId());
        rcmRecord.setControlCode(request.getControlCode().trim());
        rcmRecord.setControlName(request.getControlName().trim());
        rcmRecord.setDescription(request.getDescription());
        rcmRecord.setCategory(request.getCategory());
        rcmRecord.setModuleName(request.getModuleName());
        rcmRecord.setRiskDescription(request.getRiskDescription());
        rcmRecord.setStatus(defaultText(request.getStatus(), rcmRecord.getStatus()));
        rcmRecord.setStage(normalizeStage(request.getStage(), rcmRecord.getStage()));
        rcmRecord.setAiGenerated(Boolean.TRUE.equals(request.getAiGenerated()));
        rcmRecord.setControlObjective(request.getControlObjective());
        rcmRecord.setImplementationMethod(request.getImplementationMethod());
        rcmRecord.setEvidenceRequirement(request.getEvidenceRequirement());
        rcmRecord.setControlPerformer(request.getControlPerformer());
        rcmRecord.setControlReviewer(request.getControlReviewer());
        rcmRecord.setAdditionalOwner(request.getAdditionalOwner());
        rcmRecord.setControlRiskRating(request.getControlRiskRating());
        rcmRecord.setCurrentVersion(nextVersion(rcmRecord.getCurrentVersion()));
        rcmRecord.setUpdatedBy(currentUserAccessor.requireUserId());
        rcmRecordMapper.update(rcmRecord);
        saveSnapshot(rcmRecord, defaultText(request.getChangeSummary(), "Update RCM"));
        operationLogService.record("RCM", "UPDATE", "RCM", String.valueOf(rcmRecord.getRcmId()), rcmRecord.getControlName(), rcmRecord.getProjectId(), defaultText(request.getChangeSummary(), "Update RCM"));
        return rcmRecord;
    }

    @Transactional(rollbackFor = Exception.class)
    public RcmVersion saveVersion(Long rcmId, RcmVersionCreateRequest request) {
        RcmRecord rcmRecord = requireOwnedRcm(rcmId);
        authorizationService.requireProjectWrite(rcmRecord.getProjectId());
        saveSnapshot(rcmRecord, request.getChangeSummary());
        operationLogService.record("RCM", "SAVE_VERSION", "RCM", String.valueOf(rcmId), rcmRecord.getControlName(), rcmRecord.getProjectId(), request.getChangeSummary());
        return rcmVersionMapper.listByRcmId(rcmId).stream().findFirst().orElseThrow(() -> new BizException(BizErrorCode.RCM_SAVE_VERSION_FAILED));
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long rcmId) {
        RcmRecord rcmRecord = requireOwnedRcm(rcmId);
        authorizationService.requireProjectWrite(rcmRecord.getProjectId());
        rcmRecordMapper.softDelete(rcmId, currentUserAccessor.requireUserId());
        operationLogService.record("RCM", "DELETE", "RCM", String.valueOf(rcmId), rcmRecord.getControlName(), rcmRecord.getProjectId(), "Delete RCM");
    }

    @Transactional(rollbackFor = Exception.class)
    public RcmImportResultResponse importExcel(Long projectId, MultipartFile file) {
        authorizationService.requireProjectWrite(projectId);
        Integer operatorId = currentUserAccessor.requireUserId();
        List<RcmRecord> imported = rcmExcelService.parse(readInputStream(file), projectId, operatorId);
        List<String> errors = new ArrayList<>();
        int success = 0;
        for (RcmRecord record : imported) {
            try {
                record.setStage(SocConstants.Rcm.STAGE_MANUAL);
                record.setAiGenerated(false);
                if (record.getCurrentVersion() == null || record.getCurrentVersion().isBlank()) {
                    record.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
                }
                rcmRecordMapper.insert(record);
                saveSnapshot(record, "Import from Excel");
                success++;
            } catch (Exception exception) {
                errors.add("Control code " + record.getControlCode() + " import failed");
            }
        }
        operationLogService.record("RCM", "IMPORT", "PROJECT", String.valueOf(projectId), "RCM Import", projectId, "Import RCM Excel");
        RcmImportResultResponse response = new RcmImportResultResponse();
        response.setTotal(imported.size());
        response.setSuccess(success);
        response.setFailed(imported.size() - success);
        response.setErrors(errors);
        return response;
    }

    public void exportExcel(Long projectId, OutputStream outputStream) {
        authorizationService.requireProjectRead(projectId);
        rcmExcelService.export(rcmRecordMapper.listByProjectId(projectId), outputStream);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<RcmRecord> generateByAi(RcmAiGenerateRequest request) {
        authorizationService.requireProjectWrite(request.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        List<RcmRecord> generated = rcmAiService.generate(request, operatorId);
        for (RcmRecord record : generated) {
            record.setStage(SocConstants.Rcm.STAGE_AI_GENERATED);
            record.setSourceRequestId(request.getSourceRequestId());
            rcmRecordMapper.insert(record);
            saveSnapshot(record, SocConstants.OperationLog.Detail.RCM_SNAPSHOT_GENERATE_AI_EN);
        }
        operationLogService.record("RCM", "AI_GENERATE", "PROJECT", String.valueOf(request.getProjectId()), "AI RCM", request.getProjectId(), "Generate RCM by AI");
        return generated;
    }

    @Transactional(rollbackFor = Exception.class)
    public RcmRecord fillByAi(Long rcmId) {
        RcmRecord record = requireOwnedRcm(rcmId);
        authorizationService.requireProjectWrite(record.getProjectId());
        if (record.getControlObjective() == null || record.getControlObjective().isBlank()) {
            record.setControlObjective(SocConstants.Rcm.DraftAiText.CONTROL_OBJECTIVE_AUTOFILL_ROW);
        }
        if (record.getImplementationMethod() == null || record.getImplementationMethod().isBlank()) {
            record.setImplementationMethod(SocConstants.Rcm.DraftAiText.IMPLEMENTATION_AUTOFILL_ROW);
        }
        if (record.getEvidenceRequirement() == null || record.getEvidenceRequirement().isBlank()) {
            record.setEvidenceRequirement(SocConstants.Rcm.DraftAiText.EVIDENCE_AUTOFILL_ROW);
        }
        if (record.getControlRiskRating() == null || record.getControlRiskRating().isBlank()) {
            record.setControlRiskRating(SocConstants.Rcm.CONTROL_RISK_MEDIUM);
        }
        record.setAiGenerated(true);
        record.setCurrentVersion(nextVersion(record.getCurrentVersion()));
        record.setUpdatedBy(currentUserAccessor.requireUserId());
        rcmRecordMapper.update(record);
        saveSnapshot(record, SocConstants.OperationLog.Detail.RCM_SNAPSHOT_FILL_AI_ROW_EN);
        operationLogService.record("RCM", "FILL_BY_AI", "RCM", String.valueOf(record.getRcmId()), record.getControlName(), record.getProjectId(), "Fill single RCM by AI");
        return record;
    }

    @Transactional(rollbackFor = Exception.class)
    public RcmRecord promoteToFinal(Long rcmId) {
        return copyToStage(requireOwnedRcm(rcmId), SocConstants.Rcm.STAGE_FINAL, SocConstants.OperationLog.Detail.RCM_PROMOTE_FINAL_EN);
    }

    @Transactional(rollbackFor = Exception.class)
    public RcmRecord promoteToManual(Long rcmId) {
        return copyToStage(requireOwnedRcm(rcmId), SocConstants.Rcm.STAGE_MANUAL, SocConstants.OperationLog.Detail.RCM_PROMOTE_MANUAL_EN);
    }

    @Transactional(rollbackFor = Exception.class)
    public RcmRecord syncDraftFromRequest(Long projectId,
                                          Long requestId,
                                          String title,
                                          String ccCriteria,
                                          String requestDescription,
                                          String pointsOfFocus,
                                          Integer operatorId) {
        authorizationService.requireProjectWrite(projectId);
        RcmRecord existed = rcmRecordMapper.selectBySourceRequestAndStage(projectId, requestId, SocConstants.Rcm.STAGE_AI_GENERATED);
        if (existed == null) {
            RcmRecord draft = new RcmRecord();
            draft.setProjectId(projectId);
            draft.setControlCode(SocConstants.Rcm.CONTROL_CODE_FROM_REQUEST_PREFIX + requestId);
            draft.setControlName(title);
            draft.setDescription(requestDescription);
            draft.setCategory(ccCriteria);
            draft.setModuleName(deriveModule(ccCriteria));
            draft.setRiskDescription(defaultText(pointsOfFocus, SocConstants.Rcm.DraftAiText.RISK_PENDING_ANALYSIS));
            draft.setStatus(SocConstants.Rcm.STATUS_DRAFT);
            draft.setStage(SocConstants.Rcm.STAGE_AI_GENERATED);
            draft.setAiGenerated(true);
            draft.setSourceRequestId(requestId);
            draft.setSourceRcmId(null);
            draft.setControlObjective(SocConstants.Rcm.DraftAiText.CONTROL_OBJECTIVE_FROM_REQUEST);
            draft.setImplementationMethod(SocConstants.Rcm.DraftAiText.IMPLEMENTATION_PENDING);
            draft.setEvidenceRequirement(SocConstants.Rcm.DraftAiText.EVIDENCE_PENDING);
            draft.setControlRiskRating(SocConstants.Rcm.CONTROL_RISK_MEDIUM);
            draft.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
            draft.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
            draft.setCreatedBy(operatorId);
            draft.setUpdatedBy(operatorId);
            rcmRecordMapper.insert(draft);
            saveSnapshot(draft, SocConstants.OperationLog.Detail.RCM_SNAPSHOT_SYNC_GENERATE_EN);
            operationLogService.record("RCM", "SYNC_FROM_REQUEST", "REQUEST", String.valueOf(requestId), title, projectId, "Generate AI draft from request");
            return draft;
        }

        existed.setControlName(title);
        existed.setDescription(requestDescription);
        existed.setCategory(ccCriteria);
        existed.setModuleName(deriveModule(ccCriteria));
        existed.setRiskDescription(defaultText(pointsOfFocus, existed.getRiskDescription()));
        existed.setStatus(SocConstants.Rcm.STATUS_DRAFT);
        existed.setAiGenerated(true);
        existed.setCurrentVersion(nextVersion(existed.getCurrentVersion()));
        existed.setUpdatedBy(operatorId);
        rcmRecordMapper.update(existed);
        saveSnapshot(existed, SocConstants.OperationLog.Detail.RCM_SNAPSHOT_SYNC_UPDATE_EN);
        operationLogService.record("RCM", "SYNC_FROM_REQUEST", "REQUEST", String.valueOf(requestId), title, projectId, "Update AI draft from request");
        return existed;
    }

    private RcmRecord copyToStage(RcmRecord source, String targetStage, String actionDetail) {
        if (targetStage.equals(source.getStage())) {
            return source;
        }
        Long rootSourceId = source.getSourceRcmId() == null ? source.getRcmId() : source.getSourceRcmId();
        RcmRecord target = rcmRecordMapper.selectBySourceRcmAndStage(source.getProjectId(), rootSourceId, targetStage);
        Integer operatorId = currentUserAccessor.requireUserId();
        if (target == null) {
            target = cloneRecord(source);
            target.setStage(targetStage);
            target.setSourceRcmId(rootSourceId);
            target.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
            target.setCreatedBy(operatorId);
            target.setUpdatedBy(operatorId);
            rcmRecordMapper.insert(target);
            saveSnapshot(target, actionDetail);
        } else {
            mergeStageTarget(target, source, targetStage, operatorId);
            rcmRecordMapper.update(target);
            saveSnapshot(target, actionDetail);
        }
        operationLogService.record("RCM", "PROMOTE_STAGE", "RCM", String.valueOf(target.getRcmId()), target.getControlName(), target.getProjectId(), actionDetail);
        return target;
    }

    private void mergeStageTarget(RcmRecord target, RcmRecord source, String targetStage, Integer operatorId) {
        target.setControlCode(source.getControlCode());
        target.setControlName(source.getControlName());
        target.setDescription(source.getDescription());
        target.setCategory(source.getCategory());
        target.setModuleName(source.getModuleName());
        target.setRiskDescription(source.getRiskDescription());
        target.setStatus(source.getStatus());
        target.setStage(targetStage);
        target.setAiGenerated(source.getAiGenerated());
        target.setSourceRequestId(source.getSourceRequestId());
        target.setSourceRcmId(source.getSourceRcmId() == null ? source.getRcmId() : source.getSourceRcmId());
        target.setControlObjective(source.getControlObjective());
        target.setImplementationMethod(source.getImplementationMethod());
        target.setEvidenceRequirement(source.getEvidenceRequirement());
        target.setControlPerformer(source.getControlPerformer());
        target.setControlReviewer(source.getControlReviewer());
        target.setAdditionalOwner(source.getAdditionalOwner());
        target.setControlRiskRating(source.getControlRiskRating());
        target.setCurrentVersion(nextVersion(target.getCurrentVersion()));
        target.setUpdatedBy(operatorId);
    }

    private RcmRecord cloneRecord(RcmRecord source) {
        RcmRecord target = new RcmRecord();
        target.setProjectId(source.getProjectId());
        target.setControlCode(source.getControlCode());
        target.setControlName(source.getControlName());
        target.setDescription(source.getDescription());
        target.setCategory(source.getCategory());
        target.setModuleName(source.getModuleName());
        target.setRiskDescription(source.getRiskDescription());
        target.setStatus(source.getStatus());
        target.setAiGenerated(source.getAiGenerated());
        target.setSourceRequestId(source.getSourceRequestId());
        target.setControlObjective(source.getControlObjective());
        target.setImplementationMethod(source.getImplementationMethod());
        target.setEvidenceRequirement(source.getEvidenceRequirement());
        target.setControlPerformer(source.getControlPerformer());
        target.setControlReviewer(source.getControlReviewer());
        target.setAdditionalOwner(source.getAdditionalOwner());
        target.setControlRiskRating(source.getControlRiskRating());
        target.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        return target;
    }

    private void saveSnapshot(RcmRecord rcmRecord, String changeSummary) {
        RcmVersion version = new RcmVersion();
        version.setRcmId(rcmRecord.getRcmId());
        version.setVersionNo(rcmRecord.getCurrentVersion());
        version.setSnapshotJson(writeJson(rcmRecord));
        version.setChangeSummary(changeSummary);
        version.setCreatedBy(currentUserAccessor.requireUserId());
        rcmVersionMapper.insert(version);
    }

    private RcmRecord requireOwnedRcm(Long rcmId) {
        RcmRecord rcmRecord = rcmRecordMapper.selectById(rcmId);
        if (rcmRecord == null) {
            throw new BizException(BizErrorCode.RCM_NOT_FOUND);
        }
        authorizationService.requireProjectRead(rcmRecord.getProjectId());
        return rcmRecord;
    }

    private void validateProjectOwnership(Long projectId) {
        if (projectId != null) {
            authorizationService.requireProjectRead(projectId);
        }
    }

    private UserAccount currentUser() {
        UserAccount userAccount = userAccountMapper.selectById(currentUserAccessor.requireUserId());
        if (userAccount == null) {
            throw new BizException(BizErrorCode.AUTH_CURRENT_USER_NOT_FOUND);
        }
        return userAccount;
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
            throw new BizException(BizErrorCode.RCM_SNAPSHOT_GENERATION_FAILED);
        }
    }

    private InputStream readInputStream(MultipartFile file) {
        try {
            return file.getInputStream();
        } catch (Exception exception) {
            throw new BizException(BizErrorCode.RCM_UPLOAD_READ_FAILED);
        }
    }

    private String normalizeStage(String stage, String defaultStage) {
        String value = defaultText(stage, defaultStage).toUpperCase();
        if (SocConstants.Rcm.STAGE_MANUAL.equals(value) || SocConstants.Rcm.STAGE_FINAL.equals(value) || SocConstants.Rcm.STAGE_AI_GENERATED.equals(value)) {
            return value;
        }
        return defaultStage;
    }

    private String deriveModule(String ccCriteria) {
        String value = defaultText(ccCriteria, SocConstants.Rcm.CC_DEFAULT_SECURITY);
        if (value.toUpperCase().contains(SocConstants.Rcm.KEYWORD_AVAILABILITY)) {
            return SocConstants.Rcm.MODULE_AVAILABILITY;
        }
        if (value.toUpperCase().contains(SocConstants.Rcm.KEYWORD_PRIVACY)) {
            return SocConstants.Rcm.MODULE_PRIVACY;
        }
        if (value.toUpperCase().contains(SocConstants.Rcm.KEYWORD_CONFIDENTIAL)) {
            return SocConstants.Rcm.MODULE_CONFIDENTIALITY;
        }
        if (value.toUpperCase().contains(SocConstants.Rcm.KEYWORD_PROCESS)) {
            return SocConstants.Rcm.MODULE_PROCESSING_INTEGRITY;
        }
        return SocConstants.Rcm.MODULE_SECURITY;
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }
}
