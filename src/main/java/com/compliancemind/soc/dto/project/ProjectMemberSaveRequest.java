package com.compliancemind.soc.dto.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;

import java.util.List;

@Data
public class ProjectMemberSaveRequest {

    @Valid
    @NotEmpty(message = "成员列表不能为空")
    private List<MemberItem> members;

    @Data
    public static class MemberItem {
        @JsonAlias({"user_id"})
        private Integer userId;
        @JsonAlias({"member_role", "role", "roleCode"})
        private String memberRole;
        @JsonAlias({"display_name", "username"})
        private String displayName;
        private String email;
    }
}

