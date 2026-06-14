package com.compliancemind.soc.entity.auth;

import lombok.Data;

import java.time.LocalDateTime;

/** 登录用户账号（{@code sys_user}）。 */
@Data
public class UserAccount {

    /** 用户 ID。 */
    private Integer userId;
    /** 所属公司 ID。 */
    private Integer companyId;
    /** 显示名称。 */
    private String displayName;
    /** 邮箱（登录账号）。 */
    private String email;
    /** 手机号（登录账号）。 */
    private String phone;
    /** 头像 URL。 */
    private String avatarUrl;
    /** 职位。 */
    private String jobTitle;
    /** 密码哈希值。 */
    private String passwordHash;
    /** 公司权限代码（如 GENERAL_USER、COMP_ADMIN）。 */
    private String roleCode;
    /** 账号状态（1=启用，0=禁用）。 */
    private Integer status;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
