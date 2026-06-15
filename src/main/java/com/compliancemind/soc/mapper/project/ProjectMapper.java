package com.compliancemind.soc.mapper.project;

import com.compliancemind.soc.dto.project.ProjectQueryRequest;
import com.compliancemind.soc.entity.project.Project;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/** {@code soc_project} 合规项目。 */
@Mapper
public interface ProjectMapper {

    @Select({
        "<script>",
        "select project_id, company_id, project_name, project_info, start_date, end_date,",
        "deleted, created_by, updated_by, created_at, updated_at",
        "from soc_project",
        "where deleted = 0 and company_id = #{companyId}",
        "<if test='query.keyword != null and query.keyword != \"\"'>",
        "  and (project_name like concat('%', #{query.keyword}, '%')",
        "    or project_info like concat('%', #{query.keyword}, '%'))",
        "</if>",
        "order by updated_at desc",
        "limit #{query.offset}, #{query.pageSize}",
        "</script>"
    })
    List<Project> listAll(@Param("companyId") Integer companyId,
                          @Param("query") ProjectQueryRequest query);

    @Select({
        "<script>",
        "select count(*) as total_count",
        "from soc_project",
        "where deleted = 0 and company_id = #{companyId}",
        "<if test='query.keyword != null and query.keyword != \"\"'>",
        "  and (project_name like concat('%', #{query.keyword}, '%')",
        "    or project_info like concat('%', #{query.keyword}, '%'))",
        "</if>",
        "</script>"
    })
    long countAll(@Param("companyId") Integer companyId,
                  @Param("query") ProjectQueryRequest query);

    @Select({
        "<script>",
        "select distinct p.project_id, p.company_id, p.project_name, p.project_info, p.start_date, p.end_date,",
        "p.deleted, p.created_by, p.updated_by, p.created_at, p.updated_at",
        "from soc_project p",
        "inner join soc_project_member pm on pm.project_id = p.project_id and pm.user_id = #{userId} and pm.deleted = 0",
        "where p.deleted = 0 and p.company_id = #{companyId}",
        "<if test='query.keyword != null and query.keyword != \"\"'>",
        "  and (p.project_name like concat('%', #{query.keyword}, '%')",
        "    or p.project_info like concat('%', #{query.keyword}, '%'))",
        "</if>",
        "order by p.updated_at desc",
        "limit #{query.offset}, #{query.pageSize}",
        "</script>"
    })
    List<Project> listAllByMember(@Param("companyId") Integer companyId,
                                  @Param("userId") Integer userId,
                                  @Param("query") ProjectQueryRequest query);

    @Select({
        "<script>",
        "select count(distinct p.project_id) as total_count",
        "from soc_project p",
        "inner join soc_project_member pm on pm.project_id = p.project_id and pm.user_id = #{userId} and pm.deleted = 0",
        "where p.deleted = 0 and p.company_id = #{companyId}",
        "<if test='query.keyword != null and query.keyword != \"\"'>",
        "  and (p.project_name like concat('%', #{query.keyword}, '%')",
        "    or p.project_info like concat('%', #{query.keyword}, '%'))",
        "</if>",
        "</script>"
    })
    long countAllByMember(@Param("companyId") Integer companyId,
                          @Param("userId") Integer userId,
                          @Param("query") ProjectQueryRequest query);

    @Select("""
        select project_id, company_id, project_name, project_info, start_date, end_date,
               deleted, created_by, updated_by, created_at, updated_at
        from soc_project
        where project_id = #{projectId} and deleted = 0
        """)
    Project selectById(@Param("projectId") Long projectId);

    @Insert("""
        insert into soc_project(company_id, project_name, project_info, start_date, end_date,
                                deleted, created_by, updated_by, created_at, updated_at)
        values(#{companyId}, #{projectName}, #{projectInfo}, #{startDate}, #{endDate},
               #{deleted}, #{createdBy}, #{updatedBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "projectId")
    int insert(Project project);

    @Update("""
        update soc_project
        set project_name = #{projectName},
            project_info = #{projectInfo},
            start_date = #{startDate},
            end_date = #{endDate},
            updated_by = #{updatedBy},
            updated_at = now()
        where project_id = #{projectId} and deleted = 0
        """)
    int update(Project project);

    @Update("""
        update soc_project
        set end_date = #{endDate},
            updated_by = #{updatedBy},
            updated_at = now()
        where project_id = #{projectId} and deleted = 0
        """)
    int updateEndDate(@Param("projectId") Long projectId,
                      @Param("endDate") LocalDateTime endDate,
                      @Param("updatedBy") Integer updatedBy);

    @Update("""
        update soc_project
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where project_id = #{projectId} and deleted = 0
        """)
    int softDelete(@Param("projectId") Long projectId, @Param("updatedBy") Integer updatedBy);
}
