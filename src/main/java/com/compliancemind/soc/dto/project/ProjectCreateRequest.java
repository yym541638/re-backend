package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * 项目维度角色分配：六个固定角色可部分填写；同一 userId / role 不可重复。
     */
    @Valid
    @JsonAlias({"project_members", "roleAssignments", "members"})
    private List<ProjectMemberSaveRequest.MemberItem> members;
}
