package com.compliancemind.soc.dto.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class ProjectMemberSaveRequest {

    @Valid
    @NotEmpty(message = "成员列表不能为空")
    private List<MemberItem> members;

    @Data
    public static class MemberItem {
        @NotNull(message = "用户 ID 不能为空")
        private Integer userId;
        @NotBlank(message = "项目角色不能为空")
        @JsonAlias({"member_role", "role", "roleCode"})
        private String memberRole;
        private String displayName;
        private String email;
    }
}

