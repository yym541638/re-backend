package com.compliancemind.soc.dto.invitation;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** 登录态兑换邀请码结果。 */
@Data
public class InvitationRedeemResponse {

    @JsonProperty("project_id")
    private Long projectId;

    @JsonProperty("project_name")
    private String projectName;

    @JsonProperty("member_role")
    private String memberRole;

    @JsonProperty("already_member")
    private boolean alreadyMember;

    private String message;
}
