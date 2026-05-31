package com.compliancemind.soc.entity.controltesting;

import lombok.Data;

import java.time.LocalDateTime;

/** 控制测试主记录（{@code soc_control_test}）。 */
@Data
public class ControlTest {

    /** 控制测试 ID。 */
    private Long testId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 测试标题。 */
    private String title;
    /** 测试描述。 */
    private String description;
    /** 风险等级。 */
    private String riskLevel;
    /** 风险描述。 */
    private String riskDescription;
    /** COSO 原则。 */
    private String cosoPrinciple;
    /** 控制程序/测试步骤。 */
    private String controlProcedure;
    /** 测试结果状态（如 PENDING、PASS、FAIL）。 */
    private String resultStatus;
    /** 当前版本号（如 V1）。 */
    private String currentVersion;
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
