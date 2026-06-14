package com.compliancemind.soc.dto.project;

import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.project.ProjectMember;
import lombok.Data;

import java.util.List;

@Data
public class ProjectDetailResponse {

    private Project project;
    private List<ProjectMember> members;
    /** Project User management 六个固定角色槽位（含已分配用户）。 */
    private List<ProjectRoleSlotItem> roleSlots;
}

