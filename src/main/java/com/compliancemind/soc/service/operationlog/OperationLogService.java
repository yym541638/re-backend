package com.compliancemind.soc.service.operationlog;

import com.compliancemind.soc.common.exception.BizErrorCode;
import com.compliancemind.soc.common.exception.BizException;
import com.compliancemind.soc.dto.operationlog.OperationLogQueryRequest;
import com.compliancemind.soc.dto.operationlog.OperationLogStatisticsResponse;
import com.compliancemind.soc.entity.operationlog.OperationLog;
import com.compliancemind.soc.mapper.operationlog.OperationLogMapper;
import com.compliancemind.soc.security.AuthorizationService;
import com.compliancemind.soc.security.CurrentUserAccessor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 操作日志写入与按项目筛选查询、统计。
 */
@Service
public class OperationLogService {

    private final OperationLogMapper operationLogMapper;
    private final AuthorizationService authorizationService;
    private final CurrentUserAccessor currentUserAccessor;

    public OperationLogService(OperationLogMapper operationLogMapper,
                               AuthorizationService authorizationService,
                               CurrentUserAccessor currentUserAccessor) {
        this.operationLogMapper = operationLogMapper;
        this.authorizationService = authorizationService;
        this.currentUserAccessor = currentUserAccessor;
    }

    public List<OperationLog> list(OperationLogQueryRequest request) {
        if (request.getProjectId() == null) {
            throw new BizException(BizErrorCode.PROJECT_ID_REQUIRED);
        }
        authorizationService.requireProjectRead(request.getProjectId());
        return operationLogMapper.list(request);
    }

    public OperationLogStatisticsResponse statistics(Long projectId) {
        authorizationService.requireProjectRead(projectId);
        OperationLogStatisticsResponse response = new OperationLogStatisticsResponse();
        List<Map<String, Object>> moduleStats = operationLogMapper.countByModule(projectId);
        List<Map<String, Object>> actionStats = operationLogMapper.countByActionType(projectId);
        response.setByModule(toCountMap(moduleStats));
        response.setByActionType(toCountMap(actionStats));
        response.setTotal(response.getByActionType().values().stream().mapToLong(Long::longValue).sum());
        return response;
    }

    public void record(String moduleName,
                       String actionType,
                       String resourceType,
                       String resourceId,
                       String resourceName,
                       Long projectId,
                       String actionDetail) {
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(currentUserAccessor.requireUserId());
        operationLog.setUsername(currentUserAccessor.currentUsername());
        operationLog.setModuleName(moduleName);
        operationLog.setActionType(actionType);
        operationLog.setResourceType(resourceType);
        operationLog.setResourceId(resourceId);
        operationLog.setResourceName(resourceName);
        operationLog.setProjectId(projectId);
        operationLog.setActionDetail(actionDetail);
        operationLogMapper.insert(operationLog);
    }

    public void recordSystem(String moduleName,
                             String actionType,
                             String resourceType,
                             String resourceId,
                             String resourceName,
                             Long projectId,
                             String actionDetail) {
        OperationLog operationLog = new OperationLog();
        operationLog.setUserId(0);
        operationLog.setUsername("system");
        operationLog.setModuleName(moduleName);
        operationLog.setActionType(actionType);
        operationLog.setResourceType(resourceType);
        operationLog.setResourceId(resourceId);
        operationLog.setResourceName(resourceName);
        operationLog.setProjectId(projectId);
        operationLog.setActionDetail(actionDetail);
        operationLogMapper.insert(operationLog);
    }

    private Map<String, Long> toCountMap(List<Map<String, Object>> rows) {
        Map<String, Long> result = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String key = String.valueOf(row.get("name"));
            Number total = (Number) row.get("total");
            result.put(key, total == null ? 0L : total.longValue());
        }
        return result;
    }
}
