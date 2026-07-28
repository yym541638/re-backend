package com.compliancemind.soc.dto.invitation;

import lombok.Data;

@Data
public class InvitationQueryRequest {

    private Integer companyId;
    private Long projectId;
    private String status;
}

