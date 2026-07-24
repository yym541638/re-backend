package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/** Access Management 页：单个项目的角色配置行。 */
@Data
public class ProjectAccessMatrixItem {

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_name")
    private String projectName;

    /** 六个固定角色槽位（含已分配用户，未分配则 user 字段为空）。 */
    @JsonProperty("role_slots")
    private List<ProjectRoleSlotItem> roleSlots;
}
