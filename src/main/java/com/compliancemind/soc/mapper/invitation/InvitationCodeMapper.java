package com.compliancemind.soc.mapper.invitation;

import com.compliancemind.soc.dto.invitation.InvitationQueryRequest;
import com.compliancemind.soc.common.constants.SocConstants;
import com.compliancemind.soc.entity.invitation.InvitationCode;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** {@code sys_invitation_code} 邀请码。 */
@Mapper
public interface InvitationCodeMapper {

    @Insert("""
        insert into sys_invitation_code(code, invitation_type, company_id, project_id, member_role, status, max_uses, used_count, expires_at, remark, created_by, used_by, used_at, created_at, updated_at)
        values(#{code}, #{invitationType}, #{companyId}, #{projectId}, #{memberRole}, #{status}, #{maxUses}, #{usedCount}, #{expiresAt}, #{remark}, #{createdBy}, #{usedBy}, #{usedAt}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "invitationId")
    int insert(InvitationCode invitationCode);

    @Select("""
        select invitation_id, code, invitation_type, company_id, project_id, member_role, status, max_uses, used_count,
               expires_at, remark, created_by, used_by, used_at, created_at, updated_at
        from sys_invitation_code
        where code = #{code}
        limit 1
        """)
    InvitationCode selectByCode(@Param("code") String code);

    @Select("""
        select invitation_id, code, invitation_type, company_id, project_id, member_role, status, max_uses, used_count,
               expires_at, remark, created_by, used_by, used_at, created_at, updated_at
        from sys_invitation_code
        where invitation_id = #{invitationId}
        limit 1
        """)
    InvitationCode selectById(@Param("invitationId") Long invitationId);

    @Select("""
        <script>
        select invitation_id, code, invitation_type, company_id, project_id, member_role, status, max_uses, used_count,
               expires_at, remark, created_by, used_by, used_at, created_at, updated_at
        from sys_invitation_code
        where 1 = 1
        <if test='query.projectId != null'>
          and project_id = #{query.projectId}
        </if>
        <if test='query.status != null and query.status != ""'>
          and status = #{query.status}
        </if>
        order by invitation_id desc
        </script>
        """)
    List<InvitationCode> list(@Param("query") InvitationQueryRequest query);

    @Select("""
        select count(1)
        from sys_invitation_code
        where project_id = #{projectId}
        """)
    long countByProjectId(@Param("projectId") Long projectId);

    @Update("""
        update sys_invitation_code
        set status = #{status},
            used_count = #{usedCount},
            used_by = #{usedBy},
            used_at = #{usedAt},
            updated_at = now()
        where invitation_id = #{invitationId}
        """)
    int updateUsage(InvitationCode invitationCode);

    @Update("update sys_invitation_code set status = '" + SocConstants.Invitation.STATUS_REVOKED + "', updated_at = now() "
        + "where invitation_id = #{invitationId}")
    int revoke(@Param("invitationId") Long invitationId);
}
