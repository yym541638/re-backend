package com.compliancemind.soc.dto.invitation;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 创建公司邀请码（System Users 页）。
 * <p>无 projectId；注册时默认系统角色 {@code COMP_USER}。</p>
 */
@Data
public class CompanyInvitationCreateRequest {

    /** 可选；默认 {@code COMP_USER}。 */
    private String memberRole;

    private Integer maxUses = 1;
    private LocalDateTime expiresAt;
    private String remark;
}
