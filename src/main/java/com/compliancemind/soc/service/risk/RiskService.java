package com.compliancemind.soc.service.risk;

import com.compliancemind.soc.common.api.PageResponse;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.dto.risk.RiskCreateRequest;
import com.compliancemind.soc.dto.risk.RiskQueryRequest;
import com.compliancemind.soc.dto.risk.RiskUpdateRequest;
import com.compliancemind.soc.entity.commerce.ProductPackage;
import com.compliancemind.soc.entity.commerce.UserProduct;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.request.RequestCriteriaCatalog;
import com.compliancemind.soc.entity.risk.RiskRecord;
import com.compliancemind.soc.mapper.commerce.ProductMapper;
import com.compliancemind.soc.mapper.commerce.UserProductMapper;
import com.compliancemind.soc.mapper.request.RequestCriteriaCatalogMapper;
import com.compliancemind.soc.mapper.risk.RiskMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Risk table：项目风险清单 CRUD 与按已购范围初始化。 */
@Service
public class RiskService {

    private final RiskMapper riskMapper;
    private final RequestCriteriaCatalogMapper criteriaCatalogMapper;
    private final UserProductMapper userProductMapper;
    private final ProductMapper productMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;
    private final ObjectMapper objectMapper;

    public RiskService(RiskMapper riskMapper,
                       RequestCriteriaCatalogMapper criteriaCatalogMapper,
                       UserProductMapper userProductMapper,
                       ProductMapper productMapper,
                       AuthorizationService authorizationService,
                       CurrentUserAccessor currentUserAccessor,
                       OperationLogService operationLogService,
                       ObjectMapper objectMapper) {
        this.riskMapper = riskMapper;
        this.criteriaCatalogMapper = criteriaCatalogMapper;
        this.userProductMapper = userProductMapper;
        this.productMapper = productMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
        this.objectMapper = objectMapper;
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

    /**
     * 按公司已购 Trust Services 模块，从标准条款目录灌入 Risk table（幂等）。
     * <p>同一 {@code (cc_criteria, points_of_focus_name)} 只生成一行，避免 request catalog 多文档描述重复。</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public List<RiskRecord> generateFromCatalog(Long projectId) {
        Project project = authorizationService.requireProjectWrite(projectId);
        Set<String> modules = resolvePurchasedModules(project.getCompanyId());
        if (modules.isEmpty()) {
            throw new BizException(BizErrorCode.RISK_GENERATE_NO_PURCHASE);
        }
        List<RequestCriteriaCatalog> catalogs = criteriaCatalogMapper.listByModuleNames(modules);
        if (catalogs == null || catalogs.isEmpty()) {
            throw new BizException(BizErrorCode.RISK_GENERATE_CATALOG_EMPTY);
        }

        Integer operatorId = currentUserAccessor.requireUserId();
        // 去重：条款 + Points of Focus
        Map<String, RequestCriteriaCatalog> unique = new LinkedHashMap<>();
        for (RequestCriteriaCatalog catalog : catalogs) {
            if (catalog == null) {
                continue;
            }
            String criteria = trimToNull(catalog.getCriteriaCode());
            if (criteria == null) {
                continue;
            }
            String focus = trimToNull(catalog.getPointsOfFocus());
            String key = criteria + "\n" + (focus == null ? "" : focus);
            unique.putIfAbsent(key, catalog);
        }

        List<RiskRecord> generated = new ArrayList<>();
        for (RequestCriteriaCatalog catalog : unique.values()) {
            String criteria = trimToNull(catalog.getCriteriaCode());
            String focus = trimToNull(catalog.getPointsOfFocus());
            if (riskMapper.countByProjectCriteriaAndFocus(projectId, criteria, focus) > 0) {
                continue;
            }
            RiskRecord record = new RiskRecord();
            record.setProjectId(projectId);
            record.setCcCriteria(criteria);
            record.setCcCriteriaName(trimToNull(catalog.getRequirement()));
            record.setPointsOfFocusName(focus);
            record.setModulesName(trimToNull(catalog.getModuleName()));
            record.setRiskLevel(SocConstants.Risk.LEVEL_MEDIUM);
            record.setRiskSource(SocConstants.Risk.SOURCE_UPLOAD);
            record.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
            record.setCreatedBy(operatorId);
            record.setUpdatedBy(operatorId);
            riskMapper.insert(record);
            generated.add(riskMapper.selectById(record.getRiskId()));
        }

        if (generated.isEmpty()) {
            RiskQueryRequest query = new RiskQueryRequest();
            query.setProjectId(projectId);
            query.setPageNum(1);
            query.setPageSize(5000);
            return riskMapper.list(query, 0, 5000);
        }

        operationLogService.record(SocConstants.OperationLog.Module.RISK,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.RISK,
            String.valueOf(projectId),
            "Risk table generate",
            projectId,
            "Generate risk table from purchased criteria catalog (" + generated.size() + " rows)");
        return generated;
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

    private Set<String> resolvePurchasedModules(Integer companyId) {
        List<UserProduct> products = userProductMapper.listActiveByCompanyId(companyId);
        Set<String> modules = new LinkedHashSet<>();
        if (products == null || products.isEmpty()) {
            return modules;
        }
        for (UserProduct product : products) {
            Set<String> fromFeatures = parseFeatureModules(product.getIncludedFeatures());
            if (!fromFeatures.isEmpty()) {
                modules.addAll(fromFeatures);
                continue;
            }
            if (product.getPackageId() != null) {
                ProductPackage pkg = productMapper.selectPackageById(product.getPackageId());
                if (pkg != null) {
                    modules.addAll(parseFeatureModules(pkg.getIncludedFeatures()));
                }
            }
        }
        return modules;
    }

    private Set<String> parseFeatureModules(String featureText) {
        Set<String> modules = new LinkedHashSet<>();
        if (featureText == null || featureText.isBlank()) {
            return modules;
        }
        String trimmed = featureText.trim();
        List<String> features = new ArrayList<>();
        if (trimmed.startsWith("[")) {
            try {
                features = objectMapper.readValue(trimmed, new TypeReference<List<String>>() {
                });
            } catch (Exception ignored) {
                features = List.of();
            }
        } else {
            for (String part : trimmed.split("[,;|]")) {
                if (part != null && !part.isBlank()) {
                    features.add(part.trim());
                }
            }
        }
        for (String feature : features) {
            String normalized = normalizeModuleName(feature);
            if (normalized != null) {
                modules.add(normalized);
            }
        }
        return modules;
    }

    private String normalizeModuleName(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        String text = raw.trim();
        String upper = text.toUpperCase(Locale.ROOT).replace(' ', '_');
        return switch (upper) {
            case "SECURITY", "SEC" -> "Security";
            case "AVAILABILITY", "AVL", "AVAIL" -> "Availability";
            case "PROCESSING_INTEGRITY", "PROCESSINGINTEGRITY", "PI" -> "Processing Integrity";
            case "CONFIDENTIALITY", "CONF" -> "Confidentiality";
            case "PRIVACY", "PRIV" -> "Privacy";
            default -> text;
        };
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
