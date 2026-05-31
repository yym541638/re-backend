package com.compliancemind.soc.service.analysis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.compliancemind.soc.dto.analysis.GenerateReportRequest;
import com.compliancemind.soc.dto.analysis.ReportTaskResponse;
import com.compliancemind.soc.entity.analysis.GapAnalysisRecord;
import com.compliancemind.soc.entity.analysis.ReportTask;
import com.compliancemind.soc.entity.analysis.ScoreSnapshot;
import com.compliancemind.soc.mapper.analysis.GapAnalysisMapper;
import com.compliancemind.soc.mapper.analysis.ReportTaskMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.entity.controltesting.ControlTest;
import com.compliancemind.soc.mapper.controltesting.ControlTestMapper;
import com.compliancemind.soc.dto.operationlog.OperationLogQueryRequest;
import com.compliancemind.soc.entity.operationlog.OperationLog;
import com.compliancemind.soc.service.operationlog.OperationLogService;
import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.mapper.project.ProjectMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 报告异步任务：创建任务、轮询状态、生成 Markdown 并提供下载。
 */
@Service
public class ReportService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(SocConstants.Format.DATETIME_SECONDS);

    private final ReportTaskMapper reportTaskMapper;
    private final ProjectMapper projectMapper;
    private final GapAnalysisMapper gapAnalysisMapper;
    private final ControlTestMapper controlTestMapper;
    private final ScoreSnapshotService scoreSnapshotService;
    private final OperationLogService operationLogService;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;
    private final ObjectMapper objectMapper;

    private ExecutorService executorService;

    @Value("${app.storage.root}")
    private String storageRoot;

    public ReportService(ReportTaskMapper reportTaskMapper,
                         ProjectMapper projectMapper,
                         GapAnalysisMapper gapAnalysisMapper,
                         ControlTestMapper controlTestMapper,
                         ScoreSnapshotService scoreSnapshotService,
                         OperationLogService operationLogService,
                         AuthorizationService authorizationService,
                         CurrentUserAccessor currentUserAccessor,
                         ObjectMapper objectMapper) {
        this.reportTaskMapper = reportTaskMapper;
        this.projectMapper = projectMapper;
        this.gapAnalysisMapper = gapAnalysisMapper;
        this.controlTestMapper = controlTestMapper;
        this.scoreSnapshotService = scoreSnapshotService;
        this.operationLogService = operationLogService;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
        this.objectMapper = objectMapper;
    }

    @PostConstruct
    public void initExecutor() {
        this.executorService = Executors.newFixedThreadPool(SocConstants.ReportTask.EXECUTOR_POOL_SIZE);
    }

    public ReportTaskResponse createTask(GenerateReportRequest request) {
        authorizationService.requireProjectRead(request.getProjectId());
        Project project = projectMapper.selectById(request.getProjectId());
        if (project == null) {
            throw new BizException(BizErrorCode.PROJECT_NOT_FOUND);
        }
        ReportTask task = new ReportTask();
        task.setProjectId(request.getProjectId());
        task.setReportType(defaultText(request.getReportType(), SocConstants.ReportTask.DEFAULT_REPORT_TYPE));
        task.setFormat(defaultText(request.getFormat(), SocConstants.ReportTask.DEFAULT_FORMAT));
        task.setIncludeSectionsJson(writeJson(request.getIncludeSections()));
        task.setLanguage(defaultText(request.getLanguage(), SocConstants.ReportTask.DEFAULT_LANGUAGE));
        task.setStatus(SocConstants.ReportTask.STATUS_PENDING);
        task.setProgress(SocConstants.ReportTask.PROGRESS_INITIAL);
        task.setCreatedBy(currentUserAccessor.requireUserId());
        reportTaskMapper.insert(task);
        operationLogService.record(SocConstants.OperationLog.Module.REPORT,
            SocConstants.OperationLog.Action.CREATE_TASK,
            SocConstants.OperationLog.EntityType.PROJECT,
            String.valueOf(project.getProjectId()),
            project.getProjectName(),
            project.getProjectId(),
            SocConstants.OperationLog.Detail.REPORT_CREATE_TASK_ZH);
        executorService.submit(() -> generateTask(task.getTaskId()));
        return toResponse(reportTaskMapper.selectById(task.getTaskId()));
    }

    public ReportTaskResponse status(Long taskId) {
        ReportTask task = requireTaskVisible(taskId);
        return toResponse(task);
    }

    public void download(Long taskId, HttpServletResponse response) {
        ReportTask task = requireTaskVisible(taskId);
        if (!SocConstants.ReportTask.STATUS_SUCCESS.equalsIgnoreCase(task.getStatus()) || task.getFilePath() == null || task.getFilePath().isBlank()) {
            throw new BizException(BizErrorCode.REPORT_NOT_READY);
        }
        Path filePath = Path.of(storageRoot).resolve(task.getFilePath());
        if (!Files.exists(filePath)) {
            throw new BizException(BizErrorCode.REPORT_FILE_NOT_FOUND);
        }
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(SocConstants.ReportTask.CONTENT_TYPE_MARKDOWN_UTF8);
        response.setHeader("Content-Disposition", SocConstants.ReportTask.CONTENT_DISPOSITION_TEMPLATE + taskId + SocConstants.ReportTask.FILE_SUFFIX);
        try {
            Files.copy(filePath, response.getOutputStream());
        } catch (IOException exception) {
            throw new BizException(BizErrorCode.REPORT_DOWNLOAD_FAILED);
        }
    }

    private void generateTask(Long taskId) {
        ReportTask task = loadTask(taskId);
        try {
            updateTask(task, SocConstants.ReportTask.STATUS_PROCESSING, SocConstants.ReportTask.PROGRESS_AFTER_SNAPSHOT, null, null);
            Project project = projectMapper.selectById(task.getProjectId());
            ScoreSnapshot scoreSnapshot = scoreSnapshotService.recordSnapshot(task.getProjectId());
            updateTask(task, SocConstants.ReportTask.STATUS_PROCESSING, SocConstants.ReportTask.PROGRESS_AFTER_DATA, null, null);

            List<GapAnalysisRecord> gaps = gapAnalysisMapper.list(buildGapQuery(task.getProjectId()));
            List<ControlTest> controlTests = controlTestMapper.listByProjectId(task.getProjectId());
            OperationLogQueryRequest query = new OperationLogQueryRequest();
            query.setProjectId(task.getProjectId());
            List<OperationLog> logs = operationLogService.list(query);

            Path storageRootPath = Path.of(storageRoot);
            Path reportDir = storageRootPath.resolve(SocConstants.ReportTask.STORAGE_SUBDIR);
            Files.createDirectories(reportDir);
            Path reportPath = reportDir.resolve(SocConstants.ReportTask.FILE_PREFIX + taskId + SocConstants.ReportTask.FILE_SUFFIX);
            Files.writeString(reportPath, buildReportContent(project, scoreSnapshot, controlTests, gaps, logs), StandardCharsets.UTF_8);
            updateTask(task,
                SocConstants.ReportTask.STATUS_SUCCESS,
                SocConstants.ReportTask.PROGRESS_DONE,
                storageRootPath.relativize(reportPath).toString().replace('\\', '/'),
                null);
        } catch (Exception exception) {
            updateTask(task,
                SocConstants.ReportTask.STATUS_FAILED,
                SocConstants.ReportTask.PROGRESS_DONE,
                null,
                exception.getMessage());
        }
    }

    private String buildReportContent(Project project,
                                      ScoreSnapshot snapshot,
                                      List<ControlTest> controlTests,
                                      List<GapAnalysisRecord> gaps,
                                      List<OperationLog> logs) {
        StringBuilder builder = new StringBuilder();
        builder.append("# SOC 合规分析报告\n\n");
        builder.append("## 项目概况\n");
        builder.append("- 项目名称：").append(project.getProjectName()).append('\n');
        builder.append("- 合规类型：").append(project.getComplianceType()).append('\n');
        builder.append("- 审计类型：").append(project.getAuditType()).append('\n');
        builder.append("- 当前版本：").append(project.getCurrentVersion()).append('\n');
        builder.append('\n');

        builder.append("## 评分概览\n");
        builder.append("- 总控制项：").append(snapshot.getTotalCount()).append('\n');
        builder.append("- 通过：").append(snapshot.getPassedCount()).append('\n');
        builder.append("- 未通过：").append(snapshot.getFailedCount()).append('\n');
        builder.append("- 待处理：").append(snapshot.getPendingCount()).append('\n');
        builder.append("- 差距数量：").append(snapshot.getGapCount()).append('\n');
        builder.append("- 通过率：").append(snapshot.getPassRate()).append("%\n");
        builder.append("- 结论：").append(snapshot.getAssessment()).append('\n');
        builder.append('\n');

        builder.append("## 控制测试摘要\n");
        for (ControlTest controlTest : controlTests) {
            builder.append("- ").append(controlTest.getTitle())
                .append(" | 结果：").append(controlTest.getResultStatus())
                .append(" | 风险等级：").append(controlTest.getRiskLevel())
                .append('\n');
        }
        if (controlTests.isEmpty()) {
            builder.append("- 暂无控制测试数据\n");
        }
        builder.append('\n');

        builder.append("## 差距分析\n");
        for (GapAnalysisRecord gap : gaps) {
            builder.append("- ").append(gap.getControlTitle())
                .append(" | 差距等级：").append(gap.getGapLevel())
                .append(" | 建议：").append(gap.getRemediationSuggestion())
                .append('\n');
        }
        if (gaps.isEmpty()) {
            builder.append("- 暂无差距项\n");
        }
        builder.append('\n');

        builder.append("## 最近操作记录\n");
        for (OperationLog log : logs) {
            builder.append("- ").append(log.getCreatedAt() == null ? "" : log.getCreatedAt().format(DATE_TIME_FORMATTER))
                .append(" | ").append(defaultText(log.getModuleName(), ""))
                .append(" | ").append(defaultText(log.getActionType(), ""))
                .append(" | ").append(defaultText(log.getActionDetail(), ""))
                .append('\n');
        }
        if (logs.isEmpty()) {
            builder.append("- 暂无操作记录\n");
        }
        builder.append('\n');
        builder.append("## 结论建议\n");
        builder.append("- 优先整改高风险差距项\n");
        builder.append("- 持续补齐控制测试证据与责任人\n");
        builder.append("- 在进入正式审计前再次执行评分与差距复核\n");
        return builder.toString();
    }

    private com.compliancemind.soc.dto.analysis.GapAnalysisQueryRequest buildGapQuery(Long projectId) {
        com.compliancemind.soc.dto.analysis.GapAnalysisQueryRequest queryRequest = new com.compliancemind.soc.dto.analysis.GapAnalysisQueryRequest();
        queryRequest.setProjectId(projectId);
        return queryRequest;
    }

    private void updateTask(ReportTask task, String status, int progress, String filePath, String errorMessage) {
        task.setStatus(status);
        task.setProgress(progress);
        task.setFilePath(filePath);
        task.setErrorMessage(errorMessage);
        reportTaskMapper.updateStatus(task);
    }

    private ReportTask requireTaskVisible(Long taskId) {
        ReportTask task = loadTask(taskId);
        authorizationService.requireProjectRead(task.getProjectId());
        return task;
    }

    private ReportTask loadTask(Long taskId) {
        ReportTask task = reportTaskMapper.selectById(taskId);
        if (task == null) {
            throw new BizException(BizErrorCode.REPORT_TASK_NOT_FOUND);
        }
        return task;
    }

    private ReportTaskResponse toResponse(ReportTask task) {
        ReportTaskResponse response = new ReportTaskResponse();
        response.setTaskId(String.valueOf(task.getTaskId()));
        response.setStatus(task.getStatus());
        response.setProgress(task.getProgress());
        response.setDownloadUrl(task.getFilePath() == null ? null : "/api/analysis/download-report/" + task.getTaskId());
        return response;
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception exception) {
            return "[]";
        }
    }

    private String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
