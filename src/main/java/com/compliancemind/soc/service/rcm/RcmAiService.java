package com.compliancemind.soc.service.rcm;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.dto.rcm.RcmAiGenerateRequest;
import com.compliancemind.soc.entity.rcm.RcmRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 调用外部 LLM/Ollama HTTP API，生成或补全 RCM 字段。
 */
@Service
public class RcmAiService {

    private final ObjectMapper objectMapper;

    @Value("${app.ai.ollama-url}")
    private String ollamaUrl;

    @Value("${app.ai.model-name}")
    private String modelName;

    public RcmAiService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public boolean checkServiceAvailable() {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(ollamaUrl + "/api/tags").openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.setReadTimeout(2000);
            return connection.getResponseCode() == 200;
        } catch (Exception exception) {
            return false;
        }
    }

    public String modelName() {
        return modelName;
    }

    public List<RcmRecord> generate(RcmAiGenerateRequest request, Integer operatorId) {
        List<Map<String, String>> items = checkServiceAvailable() ? generateByOllama(request) : fallbackItems(request);
        List<RcmRecord> result = new ArrayList<>();
        for (Map<String, String> item : items) {
            RcmRecord record = new RcmRecord();
            record.setProjectId(request.getProjectId());
            record.setControlCode(item.getOrDefault("controlCode", "CC-AI-" + System.currentTimeMillis()));
            record.setControlName(item.getOrDefault("controlName", "AI 生成控制项"));
            record.setDescription(item.getOrDefault("description", ""));
            record.setCategory(item.getOrDefault("category", "logical_access"));
            record.setModuleName(item.getOrDefault("moduleName", SocConstants.Rcm.MODULE_SECURITY));
            record.setRiskDescription(item.getOrDefault("riskDescription", "风险待确认"));
            record.setStatus(SocConstants.Rcm.STATUS_AI_GENERATED);
            record.setAiGenerated(true);
            record.setControlObjective(item.getOrDefault("controlObjective", ""));
            record.setImplementationMethod(item.getOrDefault("implementationMethod", ""));
            record.setEvidenceRequirement(item.getOrDefault("evidenceRequirement", ""));
            record.setControlPerformer(item.getOrDefault("controlPerformer", ""));
            record.setControlReviewer(item.getOrDefault("controlReviewer", ""));
            record.setAdditionalOwner(item.getOrDefault("additionalOwner", ""));
            record.setControlRiskRating(item.getOrDefault("controlRiskRating", SocConstants.Ai.CONTROL_RISK_MEDIUM));
            record.setCurrentVersion(SocConstants.Project.INITIAL_VERSION);
            record.setDeleted(SocConstants.Project.SOFT_DELETE_FLAG);
            record.setCreatedBy(operatorId);
            record.setUpdatedBy(operatorId);
            result.add(record);
        }
        return result;
    }

    private List<Map<String, String>> generateByOllama(RcmAiGenerateRequest request) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(ollamaUrl + "/api/generate").openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(120000);
            connection.setDoOutput(true);

            Map<String, Object> payload = new HashMap<>();
            payload.put("model", modelName);
            payload.put("prompt", buildPrompt(request));
            payload.put("format", "json");
            payload.put("stream", false);

            try (OutputStream outputStream = connection.getOutputStream()) {
                outputStream.write(objectMapper.writeValueAsBytes(payload));
            }

            StringBuilder builder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
            }

            JsonNode responseNode = objectMapper.readTree(builder.toString());
            JsonNode contentNode = parseJsonNode(responseNode.path("response").asText());
            if (contentNode == null || !contentNode.has("rcmItems")) {
                return fallbackItems(request);
            }
            List<Map<String, String>> result = new ArrayList<>();
            for (JsonNode item : contentNode.path("rcmItems")) {
                Map<String, String> row = new HashMap<>();
                item.fields().forEachRemaining(entry -> row.put(entry.getKey(), entry.getValue().asText("")));
                result.add(row);
            }
            return result.isEmpty() ? fallbackItems(request) : result;
        } catch (Exception exception) {
            return fallbackItems(request);
        }
    }

    private JsonNode parseJsonNode(String value) {
        try {
            return objectMapper.readTree(value);
        } catch (Exception exception) {
            try {
                int start = value.indexOf('{');
                int end = value.lastIndexOf('}') + 1;
                if (start >= 0 && end > start) {
                    return objectMapper.readTree(value.substring(start, end));
                }
            } catch (Exception ignored) {
            }
            return null;
        }
    }

    private String buildPrompt(RcmAiGenerateRequest request) {
        return """
            你是一名 SOC 合规专家，请基于以下信息生成 5 条 RCM 控制项。
            公司描述：%s
            系统描述：%s
            合规框架：%s

            请严格输出 JSON：
            {
              "rcmItems": [
                {
                  "controlCode": "CC-AI-001",
                  "controlName": "访问控制策略",
                  "description": "控制描述",
                  "category": "logical_access",
                  "moduleName": "Security",
                  "riskDescription": "风险描述",
                  "controlObjective": "控制目标",
                  "implementationMethod": "实施方法",
                  "evidenceRequirement": "证据要求",
                  "controlPerformer": "控制执行人",
                  "controlReviewer": "控制复核人",
                  "additionalOwner": "额外责任人",
                  "controlRiskRating": "HIGH"
                }
              ]
            }
            只输出 JSON，不要输出解释。
            """.formatted(
            request.getCompanyDescription(),
            request.getSystemDescription(),
            request.getComplianceFramework() == null || request.getComplianceFramework().isBlank()
                ? SocConstants.Ai.DEFAULT_COMPLIANCE_FRAMEWORK
                : request.getComplianceFramework()
        );
    }

    private List<Map<String, String>> fallbackItems(RcmAiGenerateRequest request) {
        List<Map<String, String>> items = new ArrayList<>();
        items.add(buildItem("CC-AI-001", "访问控制策略", "logical_access", "Security", "未授权访问风险", request));
        items.add(buildItem("CC-AI-002", "敏感数据保护", "data_protection", "Confidentiality", "数据泄露风险", request));
        items.add(buildItem("CC-AI-003", "变更管理流程", "change_management", "Processing Integrity", "变更失控风险", request));
        items.add(buildItem("CC-AI-004", "系统监控与告警", "system_operations", "Availability", "可用性风险", request));
        items.add(buildItem("CC-AI-005", "风险评估机制", "risk_assessment", "Security", "风险识别不足", request));
        return items;
    }

    private Map<String, String> buildItem(String code, String name, String category, String moduleName, String risk, RcmAiGenerateRequest request) {
        Map<String, String> item = new HashMap<>();
        item.put("controlCode", code);
        item.put("controlName", name);
        item.put("description", "结合 " + request.getSystemDescription() + " 的场景建立 " + name);
        item.put("category", category);
        item.put("moduleName", moduleName);
        item.put("riskDescription", risk);
        item.put("controlObjective", "降低 " + risk);
        item.put("implementationMethod", "建立制度、执行流程并保留证据");
        item.put("evidenceRequirement", "制度文件、流程记录、执行凭证");
        item.put("controlPerformer", "系统负责人");
        item.put("controlReviewer", "审计负责人");
        item.put("additionalOwner", "业务负责人");
        item.put("controlRiskRating", SocConstants.Ai.CONTROL_RISK_MEDIUM);
        return item;
    }
}

