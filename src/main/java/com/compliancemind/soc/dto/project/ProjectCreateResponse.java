package com.compliancemind.soc.dto.project;

import com.compliancemind.soc.entity.project.Project;
import com.compliancemind.soc.entity.project.ProjectAttachment;
import com.compliancemind.soc.entity.project.ProjectMember;
import lombok.Data;

import java.util.List;

@Data
public class ProjectCreateResponse {

    private Project project;
    private List<ProjectMember> members;
    private List<ProjectAttachment> attachments;
}
