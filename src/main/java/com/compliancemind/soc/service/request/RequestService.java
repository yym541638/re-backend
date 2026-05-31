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
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.service.rcm.RcmService;
import com.compliancemind.soc.dto.request.RequestCreateRequest;
import com.compliancemind.soc.dto.request.RequestDetailResponse;
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
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 合规请求主数据与附件、版本快照及存储路径委托。
 */
@Service
public class RequestService {

    private static final DateTimeFormatter CODE_FORMATTER = DateTimeFormatter.ofPattern(SocConstants.Format.COMPACT_TIMESTAMP);

    private final ComplianceRequestMapper complianceRequestMapper;
    private final RequestVersionMapper requestVersionMapper;
    private final RequestAttachmentMapper requestAttachmentMapper;
    private final ProjectMapper projectMapper;
    private final UserAccountMapper userAccountMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final ObjectMapper objectMapper;
    private final LocalStorageService localStorageService;
    private final OperationLogService operationLogService;
    private final RcmService rcmService;

    public RequestService(ComplianceRequestMapper complianceRequestMapper,
                          RequestVersionMapper requestVersionMapper,
                          RequestAttachmentMapper requestAttachmentMapper,
                          ProjectMapper projectMapper,
                          UserAccountMapper userAccountMapper,
                          AuthorizationService authorizationService,
                          CurrentUserAccessor currentUserAccessor,
                          ObjectMapper objectMapper,
                          LocalStorageService localStorageService,
                          OperationLogService operationLogService,
                          RcmService rcmService) {
        this.complianceRequestMapper = complianceRequestMapper;
        this.requestVersionMapper = requestVersionMapper;
        this.requestAttachmentMapper = requestAttachmentMapper;
        this.projectMapper = projectMapper;
        this.userAccountMapper = userAccountMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.objectMapper = objectMapper;
        this.localStorageService = localStorageService;
        this.operationLogService = operationLogService;
        this.rcmService = rcmService;
    }

    public List<ComplianceRequest> list(RequestQueryRequest request) {
        if (request.getProjectId() == null) {
            throw new BizException(BizErrorCode.PROJECT_ID_REQUIRED);
        }
        authorizationService.requireProjectRead(request.getProjectId());
        return complianceRequestMapper.listAll(request);
    }

    public RequestDetailResponse detail(Long requestId) {
        ComplianceRequest complianceRequest = requireOwnedRequest(requestId);
        RequestDetailResponse response = new RequestDetailResponse();
        response.setRequest(complianceRequest);
        response.setAttachments(requestAttachmentMapper.listByRequestId(requestId));
        response.setVersions(requestVersionMapper.listByRequestId(requestId));
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    public ComplianceRequest create(RequestCreateRequest request) {
        Project project = authorizationService.requireProjectWrite(request.getProjectId());
        Integer operatorId = currentUserAccessor.requireUserId();
        ComplianceRequest complianceRequest = new ComplianceRequest();
        complianceRequest.setProjectId(project.getProjectId());
        complianceRequest.setRequestCode(SocConstants.Request.CODE_PREFIX + CODE_FORMATTER.format(LocalDateTime.now()));
        complianceRequest.setCcCriteria(request.getCcCriteria().trim());
        complianceRequest.setTitle(request.getTitle().trim());
        complianceRequest.setRequestDescription(request.getRequestDescription());
        complianceRequest.setPointsOfFocus(request.getPointsOfFocus());
        complianceRequest.setDocumentStatus(defaultText(request.getDocumentStatus(), SocConstants.Request.DOCUMENT_STATUS_PENDING));
        complianceRequest.setDocumentOwner(request.getDocumentOwner());
        complianceRequest.setImplementationDate(request.getImplementationDate());
        complianceRequest.setLastUpdateAt(LocalDateTime.now());
        complianceRequest.setNotes(request.getNotes());
        complianceRequest.setRequestor(request.getRequestor());
        complianceRequest.setComments(request.getComments());
        complianceRequest.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
        complianceRequest.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
        complianceRequest.setCreatedBy(operatorId);
        complianceRequest.setUpdatedBy(operatorId);
        complianceRequestMapper.insert(complianceRequest);
        saveSnapshot(complianceRequest, SocConstants.OperationLog.Detail.RCM_SNAPSHOT_INITIAL_VERSION_EN);
        syncRcmDraft(complianceRequest, operatorId);
        operationLogService.record(SocConstants.OperationLog.Module.REQUEST,
            SocConstants.OperationLog.Action.CREATE,
            SocConstants.OperationLog.EntityType.REQUEST,
            String.valueOf(complianceRequest.getRequestId()),
            complianceRequest.getTitle(),
            complianceRequest.getProjectId(),
            SocConstants.OperationLog.Detail.REQUEST_CREATE_EN);
        return complianceRequest;
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
        operationLogService.record(SocConstants.OperationLog.Module.REQUEST,
            SocConstants.OperationLog.Action.UPDATE,
            SocConstants.OperationLog.EntityType.REQUEST,
            String.valueOf(complianceRequest.getRequestId()),
            complianceRequest.getTitle(),
            complianceRequest.getProjectId(),
            defaultText(request.getChangeSummary(), SocConstants.OperationLog.Detail.REQUEST_UPDATE_EN));
        return complianceRequest;
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
        return requestVersionMapper.listByRequestId(requestId).stream().findFirst().orElseThrow(() -> new BizException(BizErrorCode.REQUEST_SAVE_VERSION_FAILED));
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
        syncRcmDraft(complianceRequest, operatorId);
        operationLogService.record(SocConstants.OperationLog.Module.REQUEST,
            SocConstants.OperationLog.Action.UPLOAD_ATTACHMENT,
            SocConstants.OperationLog.EntityType.REQUEST,
            String.valueOf(requestId),
            complianceRequest.getTitle(),
            complianceRequest.getProjectId(),
            SocConstants.OperationLog.Detail.REQUEST_UPLOAD_ATTACHMENT_PREFIX_EN + attachment.getFileName());
        return attachment;
    }

    @Transactional(rollbackFor = Exception.class)
    public void deleteAttachment(Long requestId, Long attachmentId) {
        ComplianceRequest complianceRequest = requireOwnedRequest(requestId);
        authorizationService.requireProjectWrite(complianceRequest.getProjectId());
        requestAttachmentMapper.softDelete(attachmentId, currentUserAccessor.requireUserId());
        syncRcmDraft(complianceRequest, currentUserAccessor.requireUserId());
        operationLogService.record(SocConstants.OperationLog.Module.REQUEST,
            SocConstants.OperationLog.Action.DELETE_ATTACHMENT,
            SocConstants.OperationLog.EntityType.REQUEST,
            String.valueOf(requestId),
            complianceRequest.getTitle(),
            complianceRequest.getProjectId(),
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
        operationLogService.record(SocConstants.OperationLog.Module.REQUEST,
            SocConstants.OperationLog.Action.DELETE,
            SocConstants.OperationLog.EntityType.REQUEST,
            String.valueOf(requestId),
            complianceRequest.getTitle(),
            complianceRequest.getProjectId(),
            SocConstants.OperationLog.Detail.REQUEST_DELETE_EN);
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
            throw new BizException(BizErrorCode.REQUEST_SNAPSHOT_GENERATION_FAILED);
        }
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value.trim();
    }

    private String extractExtension(String fileName) {
        int index = fileName.lastIndexOf('.');
        return index < 0 ? "" : fileName.substring(index + 1).toLowerCase();
    }
}
