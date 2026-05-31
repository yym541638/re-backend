package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    @JsonAlias({"project_name"})
    private String projectName;

    @NotBlank(message = "合规类型不能为空")
    @JsonAlias({"compliance_type", "type"})
    private String complianceType;

    @NotBlank(message = "审计类型不能为空")
    @JsonAlias({"audit_type"})
    private String auditType;

    @NotBlank(message = "项目状态不能为空")
    private String status;

    @JsonAlias({"start_date"})
    private LocalDate startDate;
    @JsonAlias({"end_date"})
    private LocalDate endDate;
}
