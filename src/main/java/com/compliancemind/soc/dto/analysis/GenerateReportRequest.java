package com.compliancemind.soc.dto.analysis;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class GenerateReportRequest {

    @NotNull(message = "项目不能为空")
    @JsonAlias({"project_id"})
    private Long projectId;

    @JsonAlias({"report_type"})
    private String reportType;
    private String format;
    @JsonAlias({"include_sections"})
    private List<String> includeSections;
    private String language;
}
