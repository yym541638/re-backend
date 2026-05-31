package com.compliancemind.soc.entity.auth;

import lombok.Data;

import java.time.LocalDateTime;

/** 公司主体（{@code sys_company}）。 */
@Data
public class Company {

    /** 公司 ID。 */
    private Integer companyId;
    /** 公司名称。 */
    private String companyName;
    /** 公司编码。 */
    private String companyCode;
    /** 所属行业。 */
    private String industry;
    /** 公司网站。 */
    private String website;
    /** 联系人姓名。 */
    private String contactName;
    /** 联系人电话。 */
    private String contactPhone;
    /** 公司地址。 */
    private String address;
    /** 创建时间。 */
    private LocalDateTime createdAt;
    /** 最后更新时间。 */
    private LocalDateTime updatedAt;
}
