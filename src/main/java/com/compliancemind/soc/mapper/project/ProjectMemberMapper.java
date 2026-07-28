package com.compliancemind.soc.mapper.project;

import com.compliancemind.soc.entity.project.ProjectMember;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** {@code soc_project_member} 项目成员。 */
@Mapper
public interface ProjectMemberMapper {

    @Select("""
        select member_id, project_id, user_id, member_role, display_name, email, deleted, created_by, updated_by, created_at, updated_at
        from soc_project_member
        where project_id = #{projectId} and deleted = 0
        order by member_id asc
        """)
    List<ProjectMember> listByProjectId(@Param("projectId") Long projectId);

    @Select("""
        <script>
        select member_id, project_id, user_id, member_role, display_name, email, deleted, created_by, updated_by, created_at, updated_at
        from soc_project_member
        where deleted = 0
          and project_id in
          <foreach collection="projectIds" item="projectId" open="(" separator="," close=")">
            #{projectId}
          </foreach>
        order by project_id asc, member_id asc
        </script>
        """)
    List<ProjectMember> listByProjectIds(@Param("projectIds") List<Long> projectIds);

    @Select("""
        select member_id, project_id, user_id, member_role, display_name, email, deleted, created_by, updated_by, created_at, updated_at
        from soc_project_member
        where project_id = #{projectId} and user_id = #{userId} and deleted = 0
        limit 1
        """)
    ProjectMember selectByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Integer userId);

    @Select("""
        select m.member_id, m.project_id, m.user_id, m.member_role, m.display_name, m.email, m.deleted,
               m.created_by, m.updated_by, m.created_at, m.updated_at
        from soc_project_member m
        inner join soc_project p on p.project_id = m.project_id and p.deleted = 0
        where m.deleted = 0 and m.user_id = #{userId} and p.company_id = #{companyId}
        """)
    List<ProjectMember> listByUserIdAndCompanyId(@Param("userId") Integer userId,
                                                 @Param("companyId") Integer companyId);

    @Insert("""
        insert into soc_project_member(project_id, user_id, member_role, display_name, email, deleted, created_by, updated_by, created_at, updated_at)
        values(#{projectId}, #{userId}, #{memberRole}, #{displayName}, #{email}, #{deleted}, #{createdBy}, #{updatedBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "memberId")
    int insert(ProjectMember projectMember);

    @Update("""
        update soc_project_member
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where project_id = #{projectId} and deleted = 0
        """)
    int softDeleteByProjectId(@Param("projectId") Long projectId, @Param("updatedBy") Integer updatedBy);
}
