package com.compliancemind.soc.service.request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.compliancemind.soc.entity.auth.UserAccount;
import com.compliancemind.soc.mapper.auth.UserAccountMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.common.storage.LocalStorageService;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.request.RequestMaster;
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.mapper.request.RequestMasterMapper;
import com.compliancemind.soc.service.rcm.RcmService;
import com.compliancemind.soc.dto.request.RequestCreateRequest;
import com.compliancemind.soc.dto.request.RequestDetailResponse;
import com.compliancemind.soc.dto.request.RequestDocumentOwnerItem;
import com.compliancemind.soc.dto.request.RequestEvidenceItem;
import com.compliancemind.soc.dto.request.RequestEvidenceRenameRequest;
import com.compliancemind.soc.dto.request.RequestIndividualCreateRequest;
import com.compliancemind.soc.dto.request.RequestIndividualDetailResponse;
import com.compliancemind.soc.dto.request.RequestIndividualListItem;
import com.compliancemind.soc.dto.request.RequestIndividualUpdateRequest;
import com.compliancemind.soc.dto.request.RequestQueryRequest;
import com.compliancemind.soc.dto.request.RequestUpdateRequest;
import com.compliancemind.soc.dto.request.RequestVersionCreateRequest;
import com.compliancemind.soc.entity.request.ComplianceRequest;
import com.compliancemind.soc.entity.request.RequestAttachment;
import com.compliancemind.soc.entity.request.RequestVersion;
import com.compliancemind.soc.mapper.request.ComplianceRequestMapper;
import com.compliancemind.soc.mapper.request.RequestAttachmentMapper;
import com.compliancemind.soc.mapper.request.RequestVersionMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {

    private final ComplianceRequestMapper complianceRequestMapper;
    private final RequestVersionMapper requestVersionMapper;
    private final RequestAttachmentMapper requestAttachmentMapper;
    private final RequestMasterMapper requestMasterMapper;
    private final ProjectMapper projectMapper;
    private final UserAccountMapper userAccountMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final ObjectMapper objectMapper;
    private final LocalStorageService localStorageService;
    private final OperationLogService operationLogService;
    private final RcmService rcmService;
    private final RequestAiReviewService requestAiReviewService;

    public RequestService(ComplianceRequestMapper complianceRequestMapper,
                          RequestVersionMapper requestVersionMapper,
                          RequestAttachmentMapper requestAttachmentMapper,
                          RequestMasterMapper requestMasterMapper,
                          ProjectMapper projectMapper,
                          UserAccountMapper userAccountMapper,
                          AuthorizationService authorizationService,
                          CurrentUserAccessor currentUserAccessor,
                          ObjectMapper objectMapper,
                          LocalStorageService localStorageService,
                          OperationLogService operationLogService,
                          RcmService rcmService,
                          RequestAiReviewService requestAiReviewService) {
        this.complianceRequestMapper = complianceRequestMapper;
        this.requestVersionMapper = requestVersionMapper;
        this.requestAttachmentMapper = requestAttachmentMapper;
        this.requestMasterMapper = requestMasterMapper;
        this.projectMapper = projectMapper;
        this.userAccountMapper = userAccountMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.objectMapper = objectMapper;
        this.localStorageService = localStorageService;
        this.operationLogService = operationLogService;
        this.rcmService = rcmService;
        this.requestAiReviewService = requestAiReviewService;
    }

    public List<ComplianceRequest> list(RequestQueryRequest request) {
        if (request.getProjectId() == null && request.getRequestMasterId() == null) {
            throw new BizException(BizErrorCode.PROJECT_ID_REQUIRED);
        }
        if (request.getRequestMasterId() != null) {
            RequestMaster master = requireRequestMaster(request.getRequestMasterId());
            request.setProjectId(master.getProjectId());
        } else {
            authorizationService.requireProjectRead(request.getProjectId());
        }
        return complianceRequestMapper.listAll(request);
    }

    public List<RequestIndividualListItem> listIndividuals(Long requestMasterId) {
        RequestQueryRequest query = new RequestQueryRequest();
        query.setRequestMasterId(requestMasterId);
        return complianceRequestMapper.listAll(query).stream()
            .map(this::toIndividualListItem)
            .toList();
    }

    public RequestDetailResponse detail(Long requestId) {
        ComplianceRequest complianceRequest = requireOwnedRequest(requestId);
        RequestDetailResponse response = new RequestDetailResponse();
        response.setRequest(complianceRequest);
        response.setAttachments(requestAttachmentMapper.listByRequestId(requestId));
        response.setVersions(requestVersionMapper.listByRequestId(requestId));
        return response;
    }

    public RequestIndividualDetailResponse individualDetail(Long requestId) {
        ComplianceRequest request = requireOwnedRequest(requestId);
        List<RequestAttachment> attachments = requestAttachmentMapper.listByRequestId(requestId);
        return toIndividualDetail(request, attachments);
    }

    public List<RequestDocumentOwnerItem> listDocumentOwners(Long projectId, String keyword) {
        authorizationService.requireProjectRead(projectId);
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            throw new BizException(BizErrorCode.PROJECT_NOT_FOUND);
        }
        return userAccountMapper.listUsers(project.getCompanyId(), keyword).stream()
            .map(this::toDocumentOwnerItem)
            .toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestIndividualDetailResponse createIndividual(RequestIndividualCreateRequest request) {
        RequestMaster master = requireRequestMaster(request.getRequestMasterId());
        authorizationService.requireProjectWrite(master.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();

        ComplianceRequest entity = new ComplianceRequest();
        entity.setProjectId(master.getProjectId());
        entity.setRequestMasterId(master.getRequestMasterId());
        entity.setRequestCode("TEMP");
        entity.setTitle(request.getRequestName().trim());
        entity.setCcCriteria(defaultText(request.getCcCriteria(), SocConstants.Rcm.CC_DEFAULT_SECURITY));
        entity.setPointsOfFocus(defaultText(request.getPointsOfFocus(), derivePointsOfFocus(entity.getCcCriteria())));
        entity.setRequestDescription(request.getRequestDescription());
        applyDocumentOwner(entity, request.getDocumentOwnerUserId(), request.getDocumentOwnerName(), master.getProjectId());
        entity.setRequestAssignee(request.getRequestAssignee());
        entity.setUserComment(request.getCommentContent());
        entity.setDocumentStatus(SocConstants.Request.DOCUMENT_STATUS_PENDING);
        entity.setEvidenceManualStatus(SocConstants.RequestIndividual.EVIDENCE_STATUS_PENDING);
        entity.setAiReviewStatus(SocConstants.RequestIndividual.AI_REVIEW_PENDING);
        entity.setLastUpdateAt(LocalDateTime.now());
        entity.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
        entity.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        entity.setCreatedBy(operatorId);
        entity.setUpdatedBy(operatorId);
        complianceRequestMapper.insert(entity);

        entity.setRequestCode(buildRequestCode(entity.getRequestId()));
        complianceRequestMapper.update(entity);

        saveSnapshot(entity, SocConstants.OperationLog.Detail.RCM_SNAPSHOT_INITIAL_VERSION_EN);
        syncRcmDraft(entity, operatorId);
        recordRequestLog(SocConstants.OperationLog.Action.CREATE, entity, SocConstants.OperationLog.Detail.REQUEST_CREATE_EN);
        return individualDetail(entity.getRequestId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ComplianceRequest create(RequestCreateRequest request) {
        Project project = authorizationService.requireProjectWrite(request.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        ComplianceRequest complianceRequest = new ComplianceRequest();
        complianceRequest.setProjectId(project.getProjectId());
        complianceRequest.setRequestMasterId(request.getRequestMasterId());
        complianceRequest.setRequestCode("TEMP");
        complianceRequest.setCcCriteria(request.getCcCriteria().trim());
        complianceRequest.setTitle(request.getTitle().trim());
        complianceRequest.setRequestDescription(request.getRequestDescription());
        complianceRequest.setPointsOfFocus(request.getPointsOfFocus());
        complianceRequest.setDocumentStatus(defaultText(request.getDocumentStatus(), SocConstants.Request.DOCUMENT_STATUS_PENDING));
        complianceRequest.setEvidenceManualStatus(SocConstants.RequestIndividual.EVIDENCE_STATUS_PENDING);
        complianceRequest.setDocumentOwner(request.getDocumentOwner());
        complianceRequest.setImplementationDate(request.getImplementationDate());
        complianceRequest.setLastUpdateAt(LocalDateTime.now());
        complianceRequest.setAiReviewStatus(SocConstants.RequestIndividual.AI_REVIEW_PENDING);
        complianceRequest.setNotes(request.getNotes());
        complianceRequest.setRequestor(request.getRequestor());
        complianceRequest.setComments(request.getComments());
        complianceRequest.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
        complianceRequest.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        complianceRequest.setCreatedBy(operatorId);
        complianceRequest.setUpdatedBy(operatorId);
        complianceRequestMapper.insert(complianceRequest);
        complianceRequest.setRequestCode(buildRequestCode(complianceRequest.getRequestId()));
        complianceRequestMapper.update(complianceRequest);
        saveSnapshot(complianceRequest, SocConstants.OperationLog.Detail.RCM_SNAPSHOT_INITIAL_VERSION_EN);
        syncRcmDraft(complianceRequest, operatorId);
        recordRequestLog(SocConstants.OperationLog.Action.CREATE, complianceRequest, SocConstants.OperationLog.Detail.REQUEST_CREATE_EN);
        return complianceRequest;
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestIndividualDetailResponse updateIndividual(Long requestId, RequestIndividualUpdateRequest request) {
        ComplianceRequest entity = requireOwnedRequest(requestId);
        authorizationService.requireProjectWrite(entity.getProjectId());

        entity.setTitle(request.getRequestName().trim());
        if (request.getCcCriteria() != null) {
            entity.setCcCriteria(request.getCcCriteria().trim());
        }
        if (request.getPointsOfFocus() != null) {
            entity.setPointsOfFocus(request.getPointsOfFocus());
        }
        entity.setRequestDescription(request.getRequestDescription());
        applyDocumentOwner(entity, request.getDocumentOwnerUserId(), request.getDocumentOwnerName(), entity.getProjectId());
        entity.setRequestAssignee(request.getRequestAssignee());
        entity.setUserComment(request.getCommentContent());
        if (request.getUploadEvidenceManualStatus() != null) {
            entity.setEvidenceManualStatus(request.getUploadEvidenceManualStatus().trim());
        }
        entity.setLastUpdateAt(LocalDateTime.now());
        entity.setCurrentVersion(nextVersion(entity.getCurrentVersion()));
        entity.setUpdatedBy(currentUserAccessor.requireUserId());
        complianceRequestMapper.update(entity);
        saveSnapshot(entity, SocConstants.OperationLog.Detail.REQUEST_UPDATE_EN);
        syncRcmDraft(entity, currentUserAccessor.requireUserId());
        recordRequestLog(SocConstants.OperationLog.Action.UPDATE, entity, SocConstants.OperationLog.Detail.REQUEST_UPDATE_EN);
        return individualDetail(requestId);
    }

    @Transactional(rollbackFor = Exception.class)
    public ComplianceRequest update(Long requestId, RequestUpdateRequest request) {
        ComplianceRequest complianceRequest = requireOwnedRequest(requestId);
        authorizationService.requireProjectWrite(complianceRequest.getProjectId());
        complianceRequest.setCcCriteria(request.getCcCriteria().trim());
        complianceRequest.setTitle(request.getTitle().trim());
        complianceRequest.setRequestDescription(request.getRequestDescription());
        complianceRequest.setPointsOfFocus(request.getPointsOfFocus());
        complianceRequest.setDocumentStatus(defaultText(request.getDocumentStatus(), complianceRequest.getDocumentStatus()));
        complianceRequest.setDocumentOwner(request.getDocumentOwner());
        complianceRequest.setImplementationDate(request.getImplementationDate());
        complianceRequest.setLastUpdateAt(LocalDateTime.now());
        complianceRequest.setNotes(request.getNotes());
        complianceRequest.setRequestor(request.getRequestor());
        complianceRequest.setComments(request.getComments());
        complianceRequest.setCurrentVersion(nextVersion(complianceRequest.getCurrentVersion()));
        complianceRequest.setUpdatedBy(currentUserAccessor.requireUserId());
        complianceRequestMapper.update(complianceRequest);
        saveSnapshot(complianceRequest, defaultText(request.getChangeSummary(), SocConstants.OperationLog.Detail.REQUEST_UPDATE_EN));
        syncRcmDraft(complianceRequest, currentUserAccessor.requireUserId());
        recordRequestLog(SocConstants.OperationLog.Action.UPDATE, complianceRequest,
            defaultText(request.getChangeSummary(), SocConstants.OperationLog.Detail.REQUEST_UPDATE_EN));
        return complianceRequest;
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestIndividualDetailResponse sendRequest(Long requestId) {
        ComplianceRequest entity = requireOwnedRequest(requestId);
        authorizationService.requireProjectWrite(entity.getProjectId());
        List<RequestAttachment> attachments = requestAttachmentMapper.listByRequestId(requestId);
        RequestAiReviewService.AiReviewResult review = requestAiReviewService.review(attachments);
        entity.setRequestSendDate(LocalDateTime.now());
        entity.setAiReviewStatus(review.status());
        entity.setAiReviewComment(review.comment());
        entity.setLastUpdateAt(LocalDateTime.now());
        if (!attachments.isEmpty()) {
            entity.setEvidenceManualStatus(SocConstants.RequestIndividual.EVIDENCE_STATUS_UPLOADED);
        }
        entity.setUpdatedBy(currentUserAccessor.requireUserId());
        complianceRequestMapper.update(entity);
        recordRequestLog(SocConstants.OperationLog.Action.UPDATE, entity, "Send request for AI evidence review");
        return individualDetail(requestId);
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestVersion saveVersion(Long requestId, RequestVersionCreateRequest request) {
        ComplianceRequest complianceRequest = requireOwnedRequest(requestId);
        authorizationService.requireProjectWrite(complianceRequest.getProjectId());
        saveSnapshot(complianceRequest, request.getChangeSummary());
        operationLogService.record(SocConstants.OperationLog.Module.REQUEST,
            SocConstants.OperationLog.Action.SAVE_VERSION,
            SocConstants.OperationLog.EntityType.REQUEST,
            String.valueOf(requestId),
            complianceRequest.getTitle(),
            complianceRequest.getProjectId(),
            request.getChangeSummary());
        return requestVersionMapper.listByRequestId(requestId).stream()
            .findFirst()
            .orElseThrow(() -> new BizException(BizErrorCode.REQUEST_SAVE_VERSION_FAILED));
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestAttachment uploadAttachment(Long requestId, MultipartFile file) {
        ComplianceRequest complianceRequest = requireOwnedRequest(requestId);
        authorizationService.requireProjectWrite(complianceRequest.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        LocalStorageService.StoredFile storedFile = localStorageService.storeRequestAttachment(requestId, file);
        RequestAttachment attachment = new RequestAttachment();
        attachment.setRequestId(complianceRequest.getRequestId());
        attachment.setFileName(storedFile.originalFilename());
        attachment.setFilePath(storedFile.relativePath());
        attachment.setFileType(extractExtension(storedFile.originalFilename()));
        attachment.setContentType(storedFile.contentType());
        attachment.setFileSize(storedFile.fileSize());
        attachment.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        attachment.setCreatedBy(operatorId);
        attachment.setUpdatedBy(operatorId);
        requestAttachmentMapper.insert(attachment);
        complianceRequest.setEvidenceManualStatus(SocConstants.RequestIndividual.EVIDENCE_STATUS_UPLOADED);
        complianceRequest.setLastUpdateAt(LocalDateTime.now());
        complianceRequest.setUpdatedBy(operatorId);
        complianceRequestMapper.update(complianceRequest);
        syncRcmDraft(complianceRequest, operatorId);
        recordRequestLog(SocConstants.OperationLog.Action.UPLOAD_ATTACHMENT, complianceRequest,
            SocConstants.OperationLog.Detail.REQUEST_UPLOAD_ATTACHMENT_PREFIX_EN + attachment.getFileName());
        return attachment;
    }

    @Transactional(rollbackFor = Exception.class)
    public RequestEvidenceItem renameAttachment(Long requestId, Long attachmentId, RequestEvidenceRenameRequest request) {
        ComplianceRequest complianceRequest = requireOwnedRequest(requestId);
        authorizationService.requireProjectWrite(complianceRequest.getProjectId());
        RequestAttachment attachment = requestAttachmentMapper.selectById(attachmentId);
        if (attachment == null || !requestId.equals(attachment.getRequestId())) {
            throw new BizException(BizErrorCode.REQUEST_ATTACHMENT_NOT_FOUND);
        }
        requestAttachmentMapper.updateFileName(attachmentId, request.getFileName().trim(), currentUserAccessor.requireUserId());
        attachment.setFileName(request.getFileName().trim());
        RequestEvidenceItem item = new RequestEvidenceItem();
        item.setAttachmentId(attachment.getAttachmentId());
        item.setFile(attachment.getFileName());
        item.setTime(attachment.getCreatedAt());
        return item;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long requestId, Long attachmentId) {
        ComplianceRequest complianceRequest = requireOwnedRequest(requestId);
        authorizationService.requireProjectWrite(complianceRequest.getProjectId());
        requestAttachmentMapper.softDelete(attachmentId, currentUserAccessor.requireUserId());
        syncRcmDraft(complianceRequest, currentUserAccessor.requireUserId());
        recordRequestLog(SocConstants.OperationLog.Action.DELETE_ATTACHMENT, complianceRequest,
            SocConstants.OperationLog.Detail.REQUEST_DELETE_ATTACHMENT_PREFIX_EN + attachmentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachmentById(Long attachmentId) {
        RequestAttachment attachment = requestAttachmentMapper.selectById(attachmentId);
        if (attachment == null) {
            throw new BizException(BizErrorCode.REQUEST_ATTACHMENT_NOT_FOUND);
        }
        deleteAttachment(attachment.getRequestId(), attachmentId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long requestId) {
        ComplianceRequest complianceRequest = requireOwnedRequest(requestId);
        authorizationService.requireProjectWrite(complianceRequest.getProjectId());
        complianceRequestMapper.softDelete(requestId, currentUserAccessor.requireUserId());
        recordRequestLog(SocConstants.OperationLog.Action.DELETE, complianceRequest, SocConstants.OperationLog.Detail.REQUEST_DELETE_EN);
    }

    private RequestMaster requireRequestMaster(Long requestMasterId) {
        RequestMaster master = requestMasterMapper.selectById(requestMasterId);
        if (master == null) {
            throw new BizException(BizErrorCode.REQUEST_MASTER_NOT_FOUND);
        }
        authorizationService.requireProjectRead(master.getProjectId());
        return master;
    }

    private void applyDocumentOwner(ComplianceRequest entity,
                                    Integer documentOwnerUserId,
                                    String documentOwnerName,
                                    Long projectId) {
        if (documentOwnerUserId != null) {
            Project project = projectMapper.selectById(projectId);
            UserAccount user = userAccountMapper.selectByIdAndCompanyId(documentOwnerUserId, project.getCompanyId());
            if (user == null) {
                throw new BizException(BizErrorCode.AUTH_USER_NOT_FOUND);
            }
            entity.setDocumentOwnerUserId(user.getUserId());
            entity.setDocumentOwner(firstNonBlank(documentOwnerName, user.getDisplayName()));
            return;
        }
        entity.setDocumentOwner(documentOwnerName);
    }

    private RequestIndividualListItem toIndividualListItem(ComplianceRequest request) {
        List<RequestAttachment> attachments = requestAttachmentMapper.listByRequestId(request.getRequestId());
        RequestIndividualListItem item = new RequestIndividualListItem();
        item.setRequestId(request.getRequestId());
        item.setRequestCode(request.getRequestCode());
        item.setRequestName(request.getTitle());
        item.setCcCriteria(request.getCcCriteria());
        item.setPointsOfFocus(request.getPointsOfFocus());
        item.setRequestDescription(request.getRequestDescription());
        item.setRequestCreationDate(request.getCreatedAt());
        item.setRequestAssignee(request.getRequestAssignee());
        item.setDocumentOwnerName(request.getDocumentOwner());
        item.setUploadEvidence(attachments.stream().map(RequestAttachment::getFileName).collect(Collectors.joining(", ")));
        item.setUploadEvidenceDateTime(attachments.isEmpty() ? null : attachments.get(0).getCreatedAt());
        item.setCommentContent(request.getUserComment());
        item.setUploadEvidenceManualStatus(request.getEvidenceManualStatus());
        item.setRequestSendDate(request.getRequestSendDate());
        item.setRequestIndividualReviewStatus(request.getAiReviewStatus());
        item.setRequestIndividualReviewComment(request.getAiReviewComment());
        return item;
    }

    private RequestIndividualDetailResponse toIndividualDetail(ComplianceRequest request,
                                                                 List<RequestAttachment> attachments) {
        RequestIndividualDetailResponse response = new RequestIndividualDetailResponse();
        response.setRequestId(request.getRequestId());
        response.setRequestMasterId(request.getRequestMasterId());
        response.setProjectId(request.getProjectId());
        response.setRequestCode(request.getRequestCode());
        response.setRequestName(request.getTitle());
        response.setCcCriteria(request.getCcCriteria());
        response.setPointsOfFocus(request.getPointsOfFocus());
        response.setRequestDescription(request.getRequestDescription());
        response.setRequestCreationDate(request.getCreatedAt());
        response.setDocumentOwnerName(request.getDocumentOwner());
        response.setDocumentOwnerUserId(request.getDocumentOwnerUserId());
        response.setRequestAssignee(request.getRequestAssignee());
        response.setUploadEvidenceManualStatus(request.getEvidenceManualStatus());
        response.setRequestSendDate(request.getRequestSendDate());
        response.setRequestEvidenceReviewAiStatus(request.getAiReviewStatus());
        response.setAiCommentContent(request.getAiReviewComment());
        response.setCommentContent(request.getUserComment());
        response.setEvidences(attachments.stream().map(this::toEvidenceItem).toList());
        return response;
    }

    private RequestEvidenceItem toEvidenceItem(RequestAttachment attachment) {
        RequestEvidenceItem item = new RequestEvidenceItem();
        item.setAttachmentId(attachment.getAttachmentId());
        item.setFile(attachment.getFileName());
        item.setTime(attachment.getCreatedAt());
        return item;
    }

    private RequestDocumentOwnerItem toDocumentOwnerItem(UserAccount user) {
        RequestDocumentOwnerItem item = new RequestDocumentOwnerItem();
        item.setUserId(user.getUserId());
        item.setDisplayName(user.getDisplayName());
        item.setEmail(user.getEmail());
        return item;
    }

    private String buildRequestCode(Long requestId) {
        return SocConstants.RequestIndividual.CODE_PREFIX + String.format("%06d", requestId);
    }

    private String derivePointsOfFocus(String ccCriteria) {
        if (ccCriteria == null || ccCriteria.isBlank()) {
            return "General compliance focus";
        }
        return "Points of focus for " + ccCriteria.trim();
    }

    private void syncRcmDraft(ComplianceRequest complianceRequest, Integer operatorId) {
        rcmService.syncDraftFromRequest(
            complianceRequest.getProjectId(),
            complianceRequest.getRequestId(),
            complianceRequest.getTitle(),
            complianceRequest.getCcCriteria(),
            complianceRequest.getRequestDescription(),
            complianceRequest.getPointsOfFocus(),
            operatorId
        );
    }

    private void saveSnapshot(ComplianceRequest complianceRequest, String changeSummary) {
        RequestVersion version = new RequestVersion();
        version.setRequestId(complianceRequest.getRequestId());
        version.setVersionNo(complianceRequest.getCurrentVersion());
        version.setSnapshotJson(writeJson(complianceRequest));
        version.setChangeSummary(changeSummary);
        version.setCreatedBy(currentUserAccessor.requireUserId());
        requestVersionMapper.insert(version);
    }

    private ComplianceRequest requireOwnedRequest(Long requestId) {
        ComplianceRequest complianceRequest = complianceRequestMapper.selectById(requestId);
        if (complianceRequest == null) {
            throw new BizException(BizErrorCode.REQUEST_NOT_FOUND);
        }
        authorizationService.requireProjectRead(complianceRequest.getProjectId());
        return complianceRequest;
    }

    private void recordRequestLog(String action, ComplianceRequest request, String detail) {
        operationLogService.record(SocConstants.OperationLog.Module.REQUEST,
            action,
            SocConstants.OperationLog.EntityType.REQUEST,
            String.valueOf(request.getRequestId()),
            request.getTitle(),
            request.getProjectId(),
            detail);
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
            throw new BizException(BizErrorCode.REQUEST_SNAPSHOT_GENERATION_FAILED);
        }
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String firstNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.isBlank()) {
            return preferred.trim();
        }
        return fallback;
    }

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index < 0 ? "" : fileName.substring(index + 1).toLowerCase();
    }
}
