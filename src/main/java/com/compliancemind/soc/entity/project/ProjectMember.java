package com.compliancemind.soc.entity.project;

import lombok.Data;

import java.time.LocalDateTime;

/** 项目成员及在项目内的角色（{@code soc_project_member}）。 */
@Data
public class ProjectMember {

    /** 成员记录 ID。 */
    private Long memberId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 关联用户 ID（可为空，表示仅邀请未注册）。 */
    private Integer userId;
    /** 项目内成员角色（如 MANAGER、DOCUMENT_OWNER）。 */
    private String memberRole;
    /** 成员显示名称。 */
    private String displayName;
    /** 成员邮箱。 */
    private String email;
    /** 软删除标记（0=未删除，1=已删除）。 */
    private Integer deleted;
    /** 创建人用户 ID。 */
    private Integer createdBy;
    /** 最后更新人用户 ID。 */
    private Integer updatedBy;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
