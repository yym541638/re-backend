package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ProjectCreateRequest {

    @NotBlank(message = "项目名称不能为空")
    @JsonAlias({"project_name"})
    private String projectName;

    @JsonAlias({"compliance_type", "type"})
    private String complianceType;

    @JsonAlias({"audit_type"})
    private String auditType;

    @JsonAlias({"start_date"})
    private LocalDate startDate;

    /**
     * 项目维度角色分配：从本公司用户中选取并绑定到具体项目角色。
     * 同一 userId 仅可出现一次；角色见 Project User Management 六档。
     */
    @Valid
    @JsonAlias({"project_members", "roleAssignments"})
    private List<ProjectMemberSaveRequest.MemberItem> members;
}
