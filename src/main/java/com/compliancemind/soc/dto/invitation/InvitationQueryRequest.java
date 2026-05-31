package com.compliancemind.soc.dto.invitation;

import lombok.Data;

@Data
public class InvitationQueryRequest {

    private Long projectId;
    private String status;
}

