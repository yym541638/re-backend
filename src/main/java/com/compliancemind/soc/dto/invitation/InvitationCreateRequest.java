package com.compliancemind.soc.dto.invitation;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InvitationCreateRequest {

    @NotNull(message = "项目不能为空")
    private Long projectId;

    @NotBlank(message = "成员角色不能为空")
    private String memberRole;

    private Integer maxUses = 1;
    private LocalDateTime expiresAt;
    private String remark;
}

