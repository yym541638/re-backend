package com.compliancemind.soc.dto.project;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/** Project User management 角色槽位；同一角色可有多条（每用户一条）。 */
@Data
public class ProjectRoleSlotItem {

    @JsonProperty("role_code")
    private String roleCode;

    @JsonProperty("role_name")
    private String roleName;

    @JsonProperty("user_id")
    private Integer userId;

    @JsonProperty("display_name")
    private String displayName;

    private String email;
}
