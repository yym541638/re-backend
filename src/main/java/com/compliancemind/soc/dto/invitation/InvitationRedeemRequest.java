package com.compliancemind.soc.dto.invitation;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/** Access Management「Invitation code」弹窗：已登录用户兑换邀请码。 */
@Data
public class InvitationRedeemRequest {

    @NotBlank(message = "邀请码不能为空")
    @JsonAlias({"invitation_code", "invitationCode"})
    private String code;
}
