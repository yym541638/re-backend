package com.compliancemind.soc.service.risk;

import com.compliancemind.soc.common.api.PageResponse;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.dto.risk.RiskCreateRequest;
import com.compliancemind.soc.dto.risk.RiskQueryRequest;
import com.compliancemind.soc.dto.risk.RiskUpdateRequest;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.risk.RiskRecord;
import com.compliancemind.soc.mapper.risk.RiskMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/** Risk table：项目风险清单 CRUD。 */
@Service
public class RiskService {

    private final RiskMapper riskMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;

    public RiskService(RiskMapper riskMapper,
                       AuthorizationService authorizationService,
                       CurrentUserAccessor currentUserAccessor,
                       OperationLogService operationLogService) {
        this.riskMapper = riskMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
    }

    public PageResponse<RiskRecord> list(RiskQueryRequest request) {
        if (request.getProjectId() == null) {
            throw new BizException(BizErrorCode.PROJECT_ID_REQUIRED);
        }
        authorizationService.requireProjectRead(request.getProjectId());
        int pageNum = request.getPageNum() == null || request.getPageNum() < 1 ? 1 : request.getPageNum();
        int pageSize = request.getPageSize() == null || request.getPageSize() < 1 ? 10 : request.getPageSize();
        long total = riskMapper.count(request);
        long offset = (long) (pageNum - 1) * pageSize;
        List<RiskRecord> list = riskMapper.list(request, offset, pageSize);
        return PageResponse.of(total, pageNum, pageSize, list);
    }

    public RiskRecord detail(Long riskId) {
        return requireOwnedRisk(riskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public RiskRecord create(RiskCreateRequest request) {
        Project project = authorizationService.requireProjectWrite(request.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        RiskRecord record = new RiskRecord();
        record.setProjectId(project.getProjectId());
        applyCreateFields(record, request);
        record.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        record.setCreatedBy(operatorId);
        record.setUpdatedBy(operatorId);
        riskMapper.insert(record);
        operationLogService.record(SocConstants.OperationLog.Module.RISK,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.RISK,
            String.valueOf(record.getRiskId()),
            displayName(record),
            record.getProjectId(),
            SocConstants.OperationLog.Detail.RISK_CREATE_EN);
        return riskMapper.selectById(record.getRiskId());
    }

    @Transactional(rollbackFor = Exception.class)
    public RiskRecord update(Long riskId, RiskUpdateRequest request) {
        RiskRecord record = requireOwnedRisk(riskId);
        authorizationService.requireProjectWrite(record.getProjectId());
        applyUpdateFields(record, request);
        record.setUpdatedBy(currentUserAccessor.requireUserId());
        riskMapper.update(record);
        operationLogService.record(SocConstants.OperationLog.Module.RISK,
            SocConstants.OperationLog.Action.UPDATE,
            SocConstants.OperationLog.EntityType.RISK,
            String.valueOf(riskId),
            displayName(record),
            record.getProjectId(),
            SocConstants.OperationLog.Detail.RISK_UPDATE_EN);
        return riskMapper.selectById(riskId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long riskId) {
        RiskRecord record = requireOwnedRisk(riskId);
        authorizationService.requireProjectWrite(record.getProjectId());
        riskMapper.softDelete(riskId, currentUserAccessor.requireUserId());
        operationLogService.record(SocConstants.OperationLog.Module.RISK,
            SocConstants.OperationLog.Action.DELETE,
            SocConstants.OperationLog.EntityType.RISK,
            String.valueOf(riskId),
            displayName(record),
            record.getProjectId(),
            SocConstants.OperationLog.Detail.RISK_DELETE_EN);
    }

    private RiskRecord requireOwnedRisk(Long riskId) {
        RiskRecord record = riskMapper.selectById(riskId);
        if (record == null) {
            throw new BizException(BizErrorCode.RISK_NOT_FOUND);
        }
        authorizationService.requireProjectRead(record.getProjectId());
        return record;
    }

    private void applyCreateFields(RiskRecord record, RiskCreateRequest request) {
        record.setCcCriteria(trimToNull(request.getCcCriteria()));
        record.setCycleName(trimToNull(request.getCycleName()));
        record.setModulesId(trimToNull(request.getModulesId()));
        record.setModulesName(trimToNull(request.getModulesName()));
        record.setCcCriteriaName(trimToNull(request.getCcCriteriaName()));
        record.setSubRiskId(trimToNull(request.getSubRiskId()));
        record.setSubRiskName(trimToNull(request.getSubRiskName()));
        record.setPointsOfFocusId(trimToNull(request.getPointsOfFocusId()));
        record.setPointsOfFocusName(trimToNull(request.getPointsOfFocusName()));
        record.setRiskLevel(normalizeRiskLevel(request.getRiskLevel(), SocConstants.Risk.LEVEL_MEDIUM));
        record.setRiskSource(normalizeRiskSource(request.getRiskSource(), SocConstants.Risk.SOURCE_MANUAL));
        record.setAdditionalRiskProfileDescription(trimToNull(request.getAdditionalRiskProfileDescription()));
    }

    private void applyUpdateFields(RiskRecord record, RiskUpdateRequest request) {
        if (request.getCcCriteria() != null) {
            record.setCcCriteria(trimToNull(request.getCcCriteria()));
        }
        if (request.getCycleName() != null) {
            record.setCycleName(trimToNull(request.getCycleName()));
        }
        if (request.getModulesId() != null) {
            record.setModulesId(trimToNull(request.getModulesId()));
        }
        if (request.getModulesName() != null) {
            record.setModulesName(trimToNull(request.getModulesName()));
        }
        if (request.getCcCriteriaName() != null) {
            record.setCcCriteriaName(trimToNull(request.getCcCriteriaName()));
        }
        if (request.getSubRiskId() != null) {
            record.setSubRiskId(trimToNull(request.getSubRiskId()));
        }
        if (request.getSubRiskName() != null) {
            record.setSubRiskName(trimToNull(request.getSubRiskName()));
        }
        if (request.getPointsOfFocusId() != null) {
            record.setPointsOfFocusId(trimToNull(request.getPointsOfFocusId()));
        }
        if (request.getPointsOfFocusName() != null) {
            record.setPointsOfFocusName(trimToNull(request.getPointsOfFocusName()));
        }
        if (request.getRiskLevel() != null) {
            record.setRiskLevel(normalizeRiskLevel(request.getRiskLevel(), record.getRiskLevel()));
        }
        if (request.getRiskSource() != null) {
            record.setRiskSource(normalizeRiskSource(request.getRiskSource(), record.getRiskSource()));
        }
        if (request.getAdditionalRiskProfileDescription() != null) {
            record.setAdditionalRiskProfileDescription(trimToNull(request.getAdditionalRiskProfileDescription()));
        }
    }

    private String normalizeRiskLevel(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        return switch (normalized) {
            case "HIGH", "MEDIUM", "LOW" -> normalized;
            default -> defaultValue;
        };
    }

    private String normalizeRiskSource(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT)
            .replace(' ', '_')
            .replace('-', '_');
        return switch (normalized) {
            case "MANUAL", "MANUAL_INPUT" -> SocConstants.Risk.SOURCE_MANUAL;
            case "UPLOAD" -> SocConstants.Risk.SOURCE_UPLOAD;
            case "AI", "AI_GENERATION", "AIGENERATION" -> SocConstants.Risk.SOURCE_AI_GENERATION;
            default -> defaultValue;
        };
    }

    private String displayName(RiskRecord record) {
        if (record.getSubRiskName() != null && !record.getSubRiskName().isBlank()) {
            return record.getSubRiskName();
        }
        if (record.getCcCriteria() != null && !record.getCcCriteria().isBlank()) {
            return record.getCcCriteria();
        }
        return "Risk#" + record.getRiskId();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
