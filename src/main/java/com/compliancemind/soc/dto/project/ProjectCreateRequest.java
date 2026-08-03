package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    @JsonAlias({"project_name"})
    private String projectName;

    @JsonAlias({"project_info"})
    private String projectInfo;

    @NotNull(message = "项目开始时间不能为空")
    @JsonAlias({"start_date"})
    private LocalDateTime startDate;

    @JsonAlias({"end_date"})
    private LocalDateTime endDate;
}
