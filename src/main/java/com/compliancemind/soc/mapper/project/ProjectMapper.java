package com.compliancemind.soc.mapper.project;

import com.compliancemind.soc.dto.project.ProjectQueryRequest;
import com.compliancemind.soc.entity.project.Project;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** {@code soc_project} 合规项目。 */
@Mapper
public interface ProjectMapper {

    @Select({
        "<script>",
        "select project_id, company_id, project_code, project_name, compliance_type, audit_type, current_version,",
        "gap_count, status, start_date, end_date, deleted, created_by, updated_by, created_at, updated_at",
        "from soc_project",
        "where deleted = 0 and company_id = #{companyId}",
        "<if test='query.status != null and query.status != \"\"'>",
        "  and status = #{query.status}",
        "</if>",
        "<if test='query.keyword != null and query.keyword != \"\"'>",
        "  and (project_name like concat('%', #{query.keyword}, '%') or project_code like concat('%', #{query.keyword}, '%'))",
        "</if>",
        "order by updated_at desc",
        "</script>"
    })
    List<Project> listAll(@Param("companyId") Integer companyId,
                          @Param("query") ProjectQueryRequest query);

    @Select({
        "<script>",
        "select distinct p.project_id, p.company_id, p.project_code, p.project_name, p.compliance_type, p.audit_type, p.current_version,",
        "p.gap_count, p.status, p.start_date, p.end_date, p.deleted, p.created_by, p.updated_by, p.created_at, p.updated_at",
        "from soc_project p",
        "inner join soc_project_member pm on pm.project_id = p.project_id and pm.user_id = #{userId} and pm.deleted = 0",
        "where p.deleted = 0 and p.company_id = #{companyId}",
        "<if test='query.status != null and query.status != \"\"'>",
        "  and p.status = #{query.status}",
        "</if>",
        "<if test='query.keyword != null and query.keyword != \"\"'>",
        "  and (p.project_name like concat('%', #{query.keyword}, '%') or p.project_code like concat('%', #{query.keyword}, '%'))",
        "</if>",
        "order by p.updated_at desc",
        "</script>"
    })
    List<Project> listAllByMember(@Param("companyId") Integer companyId,
                                  @Param("userId") Integer userId,
                                  @Param("query") ProjectQueryRequest query);

    @Select("""
        select project_id, company_id, project_code, project_name, compliance_type, audit_type, current_version,
               gap_count, status, start_date, end_date, deleted, created_by, updated_by, created_at, updated_at
        from soc_project
        where project_id = #{projectId} and deleted = 0
        """)
    Project selectById(@Param("projectId") Long projectId);

    @Insert("""
        insert into soc_project(company_id, project_code, project_name, compliance_type, audit_type, current_version,
                                gap_count, status, start_date, end_date, deleted, created_by, updated_by, created_at, updated_at)
        values(#{companyId}, #{projectCode}, #{projectName}, #{complianceType}, #{auditType}, #{currentVersion},
               #{gapCount}, #{status}, #{startDate}, #{endDate}, #{deleted}, #{createdBy}, #{updatedBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "projectId")
    int insert(Project project);

    @Update("""
        update soc_project
        set project_name = #{projectName},
            compliance_type = #{complianceType},
            audit_type = #{auditType},
            status = #{status},
            start_date = #{startDate},
            end_date = #{endDate},
            updated_by = #{updatedBy},
            updated_at = now()
        where project_id = #{projectId} and deleted = 0
        """)
    int update(Project project);

    @Update("""
        update soc_project
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where project_id = #{projectId} and deleted = 0
        """)
    int softDelete(@Param("projectId") Long projectId, @Param("updatedBy") Integer updatedBy);
}
