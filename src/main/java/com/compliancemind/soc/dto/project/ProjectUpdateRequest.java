package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProjectUpdateRequest {

    @NotBlank(message = "项目名称不能为空")
    @JsonAlias({"project_name"})
    private String projectName;

    @JsonAlias({"project_info"})
    private String projectInfo;

    @JsonAlias({"start_date"})
    private LocalDateTime startDate;

    @JsonAlias({"end_date"})
    private LocalDateTime endDate;

    /** 传入时全量覆盖项目成员；不传则保留现有成员。 */
    @Valid
    @JsonAlias({"project_members", "roleAssignments", "members"})
    private List<ProjectMemberSaveRequest.MemberItem> members;
}
