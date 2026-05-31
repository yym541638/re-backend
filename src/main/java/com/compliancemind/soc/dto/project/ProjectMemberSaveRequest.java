package com.compliancemind.soc.dto.project;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ProjectMemberSaveRequest {

    @Valid
    @NotEmpty(message = "成员列表不能为空")
    private List<MemberItem> members;

    @Data
    public static class MemberItem {
        private Integer userId;
        private String memberRole;
        private String displayName;
        private String email;
    }
}

