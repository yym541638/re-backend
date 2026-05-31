package com.compliancemind.soc.entity.invitation;

import lombok.Data;

import java.time.LocalDateTime;

/** 邀请注册 / 加入项目的邀请码（{@code sys_invitation_code}）。 */
@Data
public class InvitationCode {

    /** 邀请码记录 ID。 */
    private Long invitationId;
    /** 邀请码字符串（唯一）。 */
    private String code;
    /** 邀请类型（如 PROJECT）。 */
    private String invitationType;
    /** 所属公司 ID。 */
    private Integer companyId;
    /** 关联项目 ID（项目邀请时必填）。 */
    private Long projectId;
    /** 受邀成员角色。 */
    private String memberRole;
    /** 邀请码状态（如 ACTIVE、REVOKED、EXPIRED）。 */
    private String status;
    /** 最大可使用次数。 */
    private Integer maxUses;
    /** 已使用次数。 */
    private Integer usedCount;
    /** 过期时间。 */
    private LocalDateTime expiresAt;
    /** 备注。 */
    private String remark;
    /** 创建人用户 ID。 */
    private Integer createdBy;
    /** 使用人用户 ID。 */
    private Integer usedBy;
    /** 使用时间。 */
    private LocalDateTime usedAt;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
