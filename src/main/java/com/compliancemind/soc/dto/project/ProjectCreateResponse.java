package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** 创建项目成功后的最小响应。 */
@Data
public class ProjectCreateResponse {

    @JsonProperty("project_id")
    private Long projectId;
}
