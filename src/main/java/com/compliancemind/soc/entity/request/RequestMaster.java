package com.compliancemind.soc.entity.request;

import lombok.Data;

import java.time.LocalDateTime;

/** Request Master（{@code soc_request_master}）。 */
@Data
public class RequestMaster {

    /** Request Master ID。 */
    private Long requestMasterId;
    /** 所属项目 ID。 */
    private Long projectId;
    /** 业务编码（如 ReqM000001）。 */
    private String requestMasterCode;
    /** Request Master 名称。 */
    private String requestMasterName;
    /** 状态：INACTIVE / ACTIVE / COMPLETED / CANCELLED。 */
    private String status;
    /** 当前选中的版本 ID。 */
    private Long currentVersionId;
    /** 软删除标记（0=未删除，1=已删除）。 */
    private Integer deleted;
    /** 创建人用户 ID。 */
    private Integer createdBy;
    /** 最后更新人用户 ID。 */
    private Integer updatedBy;
    /** 创建时间（UI 只读 create date）。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
