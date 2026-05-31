package com.compliancemind.soc.entity.operationlog;

import lombok.Data;

import java.time.LocalDateTime;

/** 用户操作审计日志（{@code soc_operation_log}）。 */
@Data
public class OperationLog {

    /** 日志 ID。 */
    private Long logId;
    /** 操作用户 ID。 */
    private Integer userId;
    /** 操作用户名/显示名。 */
    private String username;
    /** 业务模块名（如 RCM、REQUEST）。 */
    private String moduleName;
    /** 动作类型（如 CREATE、UPDATE、DELETE）。 */
    private String actionType;
    /** 资源类型。 */
    private String resourceType;
    /** 资源 ID。 */
    private String resourceId;
    /** 资源名称。 */
    private String resourceName;
    /** 关联项目 ID。 */
    private Long projectId;
    /** 操作详情（JSON 或文本）。 */
    private String actionDetail;
    /** 操作时间。 */
    private LocalDateTime createdAt;
}
