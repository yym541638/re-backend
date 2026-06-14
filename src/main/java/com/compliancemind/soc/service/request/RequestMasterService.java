package com.compliancemind.soc.service.request;

import com.compliancemind.soc.common.api.PageResponse;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.common.storage.LocalStorageService;
import com.compliancemind.soc.dto.request.RequestIndividualCreateRequest;
import com.compliancemind.soc.dto.request.RequestIndividualDetailResponse;
import com.compliancemind.soc.dto.request.RequestIndividualListItem;
import com.compliancemind.soc.dto.request.RequestMasterCreateRequest;
import com.compliancemind.soc.dto.request.RequestMasterDetailResponse;
import com.compliancemind.soc.dto.request.RequestMasterListItem;
import com.compliancemind.soc.dto.request.RequestMasterStatusOption;
import com.compliancemind.soc.dto.request.RequestMasterTemplateFileItem;
import com.compliancemind.soc.dto.request.RequestMasterUpdateRequest;
import com.compliancemind.soc.dto.request.RequestMasterVersionDetailResponse;
import com.compliancemind.soc.dto.request.RequestMasterVersionListItem;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.request.RequestMaster;
import com.compliancemind.soc.entity.request.RequestMasterTemplateFile;
import com.compliancemind.soc.entity.request.RequestMasterVersion;
import com.compliancemind.soc.mapper.request.ComplianceRequestMapper;
import com.compliancemind.soc.mapper.request.RequestMasterMapper;
import com.compliancemind.soc.mapper.request.RequestMasterTemplateFileMapper;
import com.compliancemind.soc.mapper.request.RequestMasterVersionMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Request Master 业务（PRD 2.5.2）：列表、详情、CRUD。
 */
@Service
public class RequestMasterService {

    private static final DateTimeFormatter VERSION_LABEL_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    private static final List<RequestMasterStatusOption> STATUS_OPTIONS = List.of(
        option(SocConstants.RequestMaster.STATUS_COMPLETED,
            "Completed - completed request (no further action allowed)"),
        option(SocConstants.RequestMaster.STATUS_CANCELLED,
            "Cancelled - unable to modify"),
        option(SocConstants.RequestMaster.STATUS_ACTIVE,
            "Active - sent invitation email to document owner"),
        option(SocConstants.RequestMaster.STATUS_INACTIVE,
            "Inactive - haven't sent invitation email to document owner")
    );

    private final RequestMasterMapper requestMasterMapper;
    private final RequestMasterTemplateFileMapper templateFileMapper;
    private final RequestMasterVersionMapper versionMapper;
    private final ComplianceRequestMapper complianceRequestMapper;
    private final RequestService requestService;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final OperationLogService operationLogService;
    private final LocalStorageService localStorageService;
    private final ObjectMapper objectMapper;

    public RequestMasterService(RequestMasterMapper requestMasterMapper,
                                RequestMasterTemplateFileMapper templateFileMapper,
                                RequestMasterVersionMapper versionMapper,
                                ComplianceRequestMapper complianceRequestMapper,
                                RequestService requestService,
                                AuthorizationService authorizationService,
                                CurrentUserAccessor currentUserAccessor,
                                OperationLogService operationLogService,
                                LocalStorageService localStorageService,
                                ObjectMapper objectMapper) {
        this.requestMasterMapper = requestMasterMapper;
        this.templateFileMapper = templateFileMapper;
        this.versionMapper = versionMapper;
        this.complianceRequestMapper = complianceRequestMapper;
        this.requestService = requestService;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.operationLogService = operationLogService;
        this.localStorageService = localStorageService;
        this.objectMapper = objectMapper;
    }

    public List<RequestMasterListItem> list(Long projectId) {
        if (projectId == null) {
            throw new BizException(BizErrorCode.PROJECT_ID_REQUIRED);
        }
        authorizationService.requireProjectRead(projectId);
        return requestMasterMapper.listByProjectId(projectId).stream()
            .map(this::toListItem)
            .toList();
    }

    public List<RequestMasterStatusOption> listStatusOptions() {
        return STATUS_OPTIONS;
    }

    public RequestMasterDetailResponse detail(Long requestMasterId) {
        RequestMaster requestMaster = requireOwnedRequestMaster(requestMasterId);
        return toDetailResponse(requestMaster);
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestMasterDetailResponse create(RequestMasterCreateRequest request) {
        Project project = authorizationService.requireProjectWrite(request.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();

        RequestMaster requestMaster = new RequestMaster();
        requestMaster.setProjectId(project.getProjectId());
        requestMaster.setRequestMasterCode("TEMP");
        requestMaster.setRequestMasterName(request.getRequestMasterName().trim());
        requestMaster.setStatus(resolveStatus(request.getRequestMasterStatus(),
            SocConstants.RequestMaster.STATUS_INACTIVE));
        requestMaster.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        requestMaster.setCreatedBy(operatorId);
        requestMaster.setUpdatedBy(operatorId);
        requestMasterMapper.insert(requestMaster);

        requestMaster.setRequestMasterCode(buildRequestMasterCode(requestMaster.getRequestMasterId()));
        requestMasterMapper.updateCode(requestMaster);

        operationLogService.record(SocConstants.OperationLog.Module.REQUEST_MASTER,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.REQUEST_MASTER,
            String.valueOf(requestMaster.getRequestMasterId()),
            requestMaster.getRequestMasterName(),
            project.getProjectId(),
            SocConstants.OperationLog.Detail.REQUEST_MASTER_CREATE_EN);

        return toDetailResponse(requestMaster);
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestMasterDetailResponse update(Long requestMasterId, RequestMasterUpdateRequest request) {
        RequestMaster requestMaster = requireOwnedRequestMaster(requestMasterId);
        assertModifiable(requestMaster);

        authorizationService.requireProjectWrite(requestMaster.getProjectId());
        requestMaster.setRequestMasterName(request.getRequestMasterName().trim());
        if (request.getRequestMasterStatus() != null && !request.getRequestMasterStatus().isBlank()) {
            requestMaster.setStatus(resolveStatus(request.getRequestMasterStatus(), requestMaster.getStatus()));
        }
        requestMaster.setUpdatedBy(currentUserAccessor.requireUserId());
        requestMasterMapper.update(requestMaster);

        operationLogService.record(SocConstants.OperationLog.Module.REQUEST_MASTER,
            SocConstants.OperationLog.Action.UPDATE,
            SocConstants.OperationLog.EntityType.REQUEST_MASTER,
            String.valueOf(requestMaster.getRequestMasterId()),
            requestMaster.getRequestMasterName(),
            requestMaster.getProjectId(),
            SocConstants.OperationLog.Detail.REQUEST_MASTER_UPDATE_EN);

        return detail(requestMasterId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long requestMasterId) {
        RequestMaster requestMaster = requireOwnedRequestMaster(requestMasterId);
        authorizationService.requireProjectWrite(requestMaster.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        requestMasterMapper.softDelete(requestMasterId, operatorId);
        operationLogService.record(SocConstants.OperationLog.Module.REQUEST_MASTER,
            SocConstants.OperationLog.Action.DELETE,
            SocConstants.OperationLog.EntityType.REQUEST_MASTER,
            String.valueOf(requestMasterId),
            requestMaster.getRequestMasterName(),
            requestMaster.getProjectId(),
            SocConstants.OperationLog.Detail.REQUEST_MASTER_DELETE_EN);
    }

    public List<RequestIndividualListItem> listIndividuals(Long requestMasterId) {
        requireOwnedRequestMaster(requestMasterId);
        return requestService.listIndividuals(requestMasterId);
    }

    public PageResponse<RequestMasterTemplateFileItem> listTemplateFiles(Long requestMasterId,
                                                                           Integer pageNum,
                                                                           Integer pageSize) {
        requireOwnedRequestMaster(requestMasterId);
        int resolvedPageNum = pageNum == null || pageNum < 1 ? 1 : pageNum;
        int resolvedPageSize = pageSize == null || pageSize < 1 ? 10 : pageSize;
        long total = templateFileMapper.countByMasterId(requestMasterId);
        long offset = (long) (resolvedPageNum - 1) * resolvedPageSize;
        List<RequestMasterTemplateFileItem> list = templateFileMapper
            .listByMasterId(requestMasterId, offset, resolvedPageSize).stream()
            .map(this::toTemplateFileItem)
            .toList();
        return PageResponse.of(total, resolvedPageNum, resolvedPageSize, list);
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestMasterTemplateFileItem uploadTemplateFile(Long requestMasterId,
                                                            MultipartFile file,
                                                            String relevantCriteria) {
        RequestMaster master = requireOwnedRequestMaster(requestMasterId);
        authorizationService.requireProjectWrite(master.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        LocalStorageService.StoredFile storedFile =
            localStorageService.storeRequestMasterTemplateFile(requestMasterId, file);
        RequestMasterTemplateFile entity = new RequestMasterTemplateFile();
        entity.setRequestMasterId(requestMasterId);
        entity.setFileNo(templateFileMapper.maxFileNo(requestMasterId) + 1);
        entity.setFileName(storedFile.originalFilename());
        entity.setFilePath(storedFile.relativePath());
        entity.setRelevantCriteria(relevantCriteria);
        entity.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        templateFileMapper.insert(entity);
        return toTemplateFileItem(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplateFile(Long requestMasterId, Long templateFileId) {
        RequestMaster master = requireOwnedRequestMaster(requestMasterId);
        authorizationService.requireProjectWrite(master.getProjectId());
        RequestMasterTemplateFile file = templateFileMapper.selectById(templateFileId);
        if (file == null || !requestMasterId.equals(file.getRequestMasterId())) {
            throw new BizException(BizErrorCode.REQUEST_MASTER_TEMPLATE_NOT_FOUND);
        }
        templateFileMapper.softDelete(templateFileId, currentUserAccessor.requireUserId());
    }

    public byte[] downloadTemplateFile() {
        try (InputStream inputStream = new ClassPathResource("templates/request_individual_template.csv")
            .getInputStream()) {
            return inputStream.readAllBytes();
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.STORAGE_SAVE_FAILED);
        }
    }

    public List<RequestMasterVersionListItem> listVersions(Long requestMasterId) {
        requireOwnedRequestMaster(requestMasterId);
        return versionMapper.listByMasterId(requestMasterId).stream()
            .map(this::toVersionListItem)
            .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestMasterVersionListItem saveVersion(Long requestMasterId) {
        RequestMaster master = requireOwnedRequestMaster(requestMasterId);
        authorizationService.requireProjectWrite(master.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        List<RequestIndividualListItem> snapshot = requestService.listIndividuals(requestMasterId);
        RequestMasterVersion version = new RequestMasterVersion();
        version.setRequestMasterId(requestMasterId);
        version.setVersionLabel(VERSION_LABEL_FORMATTER.format(LocalDateTime.now()));
        version.setSnapshotJson(writeJson(snapshot));
        version.setIsLatest(1);
        version.setCreatedBy(operatorId);
        versionMapper.clearLatest(requestMasterId);
        versionMapper.insert(version);
        requestMasterMapper.updateCurrentVersion(requestMasterId, version.getVersionId(), operatorId);
        return toVersionListItem(version);
    }

    public RequestMasterVersionDetailResponse getVersionDetail(Long requestMasterId, Long versionId) {
        requireOwnedRequestMaster(requestMasterId);
        RequestMasterVersion version = versionMapper.selectById(versionId);
        if (version == null || !requestMasterId.equals(version.getRequestMasterId())) {
            throw new BizException(BizErrorCode.REQUEST_MASTER_VERSION_NOT_FOUND);
        }
        RequestMasterVersionDetailResponse response = new RequestMasterVersionDetailResponse();
        response.setVersionId(version.getVersionId());
        response.setVersionLabel(version.getVersionLabel());
        response.setIndividuals(readSnapshot(version.getSnapshotJson()));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public List<RequestIndividualListItem> generateIndividuals(Long requestMasterId) {
        RequestMaster master = requireOwnedRequestMaster(requestMasterId);
        authorizationService.requireProjectWrite(master.getProjectId());
        List<RequestMasterTemplateFile> templates = templateFileMapper.listAllByMasterId(requestMasterId);
        if (templates.isEmpty()) {
            throw new BizException(BizErrorCode.REQUEST_MASTER_GENERATE_EMPTY);
        }
        List<RequestIndividualListItem> generated = new ArrayList<>();
        for (RequestMasterTemplateFile template : templates) {
            String criteria = defaultCriteria(template);
            if (complianceRequestMapper.countByMasterAndCriteria(requestMasterId, criteria) > 0) {
                continue;
            }
            RequestIndividualCreateRequest createRequest = new RequestIndividualCreateRequest();
            createRequest.setRequestMasterId(requestMasterId);
            createRequest.setRequestName(buildGeneratedName(template, criteria));
            createRequest.setCcCriteria(criteria);
            createRequest.setPointsOfFocus("Points of focus for " + criteria);
            createRequest.setRequestDescription("Auto-generated from template file: " + template.getFileName());
            generated.add(toIndividualListItem(requestService.createIndividual(createRequest)));
        }
        if (generated.isEmpty()) {
            generated = requestService.listIndividuals(requestMasterId);
        }
        return generated;
    }

    private RequestIndividualListItem toIndividualListItem(RequestIndividualDetailResponse detail) {
        RequestIndividualListItem item = new RequestIndividualListItem();
        item.setRequestId(detail.getRequestId());
        item.setRequestCode(detail.getRequestCode());
        item.setRequestName(detail.getRequestName());
        item.setCcCriteria(detail.getCcCriteria());
        item.setPointsOfFocus(detail.getPointsOfFocus());
        item.setRequestDescription(detail.getRequestDescription());
        item.setRequestCreationDate(detail.getRequestCreationDate());
        item.setRequestAssignee(detail.getRequestAssignee());
        item.setDocumentOwnerName(detail.getDocumentOwnerName());
        item.setUploadEvidenceManualStatus(detail.getUploadEvidenceManualStatus());
        item.setRequestSendDate(detail.getRequestSendDate());
        item.setRequestIndividualReviewStatus(detail.getRequestEvidenceReviewAiStatus());
        item.setRequestIndividualReviewComment(detail.getAiCommentContent());
        item.setCommentContent(detail.getCommentContent());
        return item;
    }

    private String defaultCriteria(RequestMasterTemplateFile template) {
        if (template.getRelevantCriteria() != null && !template.getRelevantCriteria().isBlank()) {
            return template.getRelevantCriteria().trim();
        }
        return "CC" + template.getFileNo();
    }

    private String buildGeneratedName(RequestMasterTemplateFile template, String criteria) {
        if (template.getFileName() != null && !template.getFileName().isBlank()) {
            int dot = template.getFileName().lastIndexOf('.');
            return dot > 0 ? template.getFileName().substring(0, dot) : template.getFileName();
        }
        return criteria + " Request";
    }

    private RequestMasterTemplateFileItem toTemplateFileItem(RequestMasterTemplateFile file) {
        RequestMasterTemplateFileItem item = new RequestMasterTemplateFileItem();
        item.setTemplateFileId(file.getTemplateFileId());
        item.setFileNo(file.getFileNo());
        item.setFiles(file.getFileName());
        item.setRelevantCriteria(file.getRelevantCriteria());
        return item;
    }

    private RequestMasterVersionListItem toVersionListItem(RequestMasterVersion version) {
        RequestMasterVersionListItem item = new RequestMasterVersionListItem();
        item.setVersionId(version.getVersionId());
        item.setVersionLabel(version.getVersionLabel());
        item.setLatest(version.getIsLatest() != null && version.getIsLatest() == 1);
        item.setCreatedAt(version.getCreatedAt());
        return item;
    }

    private List<RequestIndividualListItem> readSnapshot(String snapshotJson) {
        try {
            return objectMapper.readValue(snapshotJson, new TypeReference<List<RequestIndividualListItem>>() {
            });
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.REQUEST_SNAPSHOT_GENERATION_FAILED);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.REQUEST_SNAPSHOT_GENERATION_FAILED);
        }
    }

    private RequestMaster requireOwnedRequestMaster(Long requestMasterId) {
        if (requestMasterId == null) {
            throw new BizException(BizErrorCode.REQUEST_MASTER_NOT_FOUND);
        }
        RequestMaster requestMaster = requestMasterMapper.selectById(requestMasterId);
        if (requestMaster == null) {
            throw new BizException(BizErrorCode.REQUEST_MASTER_NOT_FOUND);
        }
        authorizationService.requireProjectRead(requestMaster.getProjectId());
        return requestMaster;
    }

    private void assertModifiable(RequestMaster requestMaster) {
        String status = normalizeStatusToken(requestMaster.getStatus());
        if (SocConstants.RequestMaster.STATUS_COMPLETED.equals(status)
            || SocConstants.RequestMaster.STATUS_CANCELLED.equals(status)) {
            throw new BizException(BizErrorCode.REQUEST_MASTER_NOT_MODIFIABLE);
        }
    }

    private String resolveStatus(String rawStatus, String defaultStatus) {
        if (rawStatus == null || rawStatus.isBlank()) {
            return defaultStatus;
        }
        String normalized = normalizeStatusToken(rawStatus);
        return switch (normalized) {
            case "INACTIVE" -> SocConstants.RequestMaster.STATUS_INACTIVE;
            case "ACTIVE" -> SocConstants.RequestMaster.STATUS_ACTIVE;
            case "COMPLETED", "COMPLETE" -> SocConstants.RequestMaster.STATUS_COMPLETED;
            case "CANCELLED", "CANCELED" -> SocConstants.RequestMaster.STATUS_CANCELLED;
            default -> throw new BizException(BizErrorCode.REQUEST_MASTER_STATUS_UNSUPPORTED);
        };
    }

    private String normalizeStatusToken(String status) {
        if (status == null) {
            return "";
        }
        return status.trim()
            .replace('-', '_')
            .replace(' ', '_')
            .toUpperCase(Locale.ROOT);
    }

    private String buildRequestMasterCode(Long requestMasterId) {
        return SocConstants.RequestMaster.CODE_PREFIX + String.format("%06d", requestMasterId);
    }

    private RequestMasterListItem toListItem(RequestMaster requestMaster) {
        RequestMasterListItem item = new RequestMasterListItem();
        item.setRequestMasterId(requestMaster.getRequestMasterId());
        item.setRequestId(requestMaster.getRequestMasterCode());
        item.setRequestMasterName(requestMaster.getRequestMasterName());
        item.setRequestMasterCreateDate(requestMaster.getCreatedAt());
        item.setRequestMasterStatus(requestMaster.getStatus());
        return item;
    }

    private RequestMasterDetailResponse toDetailResponse(RequestMaster requestMaster) {
        RequestMasterDetailResponse response = new RequestMasterDetailResponse();
        response.setRequestMasterId(requestMaster.getRequestMasterId());
        response.setProjectId(requestMaster.getProjectId());
        response.setRequestId(requestMaster.getRequestMasterCode());
        response.setRequestMasterName(requestMaster.getRequestMasterName());
        response.setCreateDate(requestMaster.getCreatedAt());
        response.setRequestMasterStatus(requestMaster.getStatus());
        return response;
    }

    private static RequestMasterStatusOption option(String status, String label) {
        RequestMasterStatusOption option = new RequestMasterStatusOption();
        option.setStatus(status);
        option.setLabel(label);
        return option;
    }
}
