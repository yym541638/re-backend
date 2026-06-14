package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;

/** 项目列表行，对应 UI 表格列。 */
@Data
public class ProjectListItem {

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("project_info")
    private String projectInfo;

    @JsonProperty("start_date")
    private LocalDateTime startDate;

    @JsonProperty("end_date")
    private LocalDateTime endDate;

    @JsonProperty("last_modified_date")
    @JsonAlias({"updated_at"})
    private LocalDateTime lastModifiedDate;
}
