package com.compliancemind.soc.mapper.controltesting;

import com.compliancemind.soc.dto.controltesting.ControlTestQueryRequest;
import com.compliancemind.soc.entity.controltesting.ControlTest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;
import java.util.Map;

/** {@code soc_control_test} 控制测试主表。 */
@Mapper
public interface ControlTestMapper {

    @Select({
        "<script>",
        "select test_id, project_id, title, description, risk_level, risk_description, coso_principle, control_procedure,",
        "result_status, current_version, deleted, created_by, updated_by, created_at, updated_at",
        "from soc_control_test",
        "where deleted = 0",
        "<if test='query.projectId != null'>",
        "  and project_id = #{query.projectId}",
        "</if>",
        "<if test='query.resultStatus != null and query.resultStatus != \"\"'>",
        "  and result_status = #{query.resultStatus}",
        "</if>",
        "<if test='query.riskLevel != null and query.riskLevel != \"\"'>",
        "  and risk_level = #{query.riskLevel}",
        "</if>",
        "order by updated_at desc",
        "</script>"
    })
    List<ControlTest> list(@Param("query") ControlTestQueryRequest query);

    @Select({
        "<script>",
        "select count(1) from soc_control_test where deleted = 0",
        "<if test='query.projectId != null'>",
        "  and project_id = #{query.projectId}",
        "</if>",
        "<if test='query.resultStatus != null and query.resultStatus != \"\"'>",
        "  and result_status = #{query.resultStatus}",
        "</if>",
        "<if test='query.riskLevel != null and query.riskLevel != \"\"'>",
        "  and risk_level = #{query.riskLevel}",
        "</if>",
        "</script>"
    })
    long count(@Param("query") ControlTestQueryRequest query);

    @Select("""
        select test_id, project_id, title, description, risk_level, risk_description, coso_principle, control_procedure,
               result_status, current_version, deleted, created_by, updated_by, created_at, updated_at
        from soc_control_test
        where test_id = #{testId} and deleted = 0
        """)
    ControlTest selectById(@Param("testId") Long testId);

    @Insert("""
        insert into soc_control_test(project_id, title, description, risk_level, risk_description, coso_principle, control_procedure,
                                     result_status, current_version, deleted, created_by, updated_by, created_at, updated_at)
        values(#{projectId}, #{title}, #{description}, #{riskLevel}, #{riskDescription}, #{cosoPrinciple}, #{controlProcedure},
               #{resultStatus}, #{currentVersion}, #{deleted}, #{createdBy}, #{updatedBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "testId")
    int insert(ControlTest controlTest);

    @Update("""
        update soc_control_test
        set title = #{title},
            description = #{description},
            risk_level = #{riskLevel},
            risk_description = #{riskDescription},
            coso_principle = #{cosoPrinciple},
            control_procedure = #{controlProcedure},
            result_status = #{resultStatus},
            current_version = #{currentVersion},
            updated_by = #{updatedBy},
            updated_at = now()
        where test_id = #{testId} and deleted = 0
        """)
    int update(ControlTest controlTest);

    @Update("""
        update soc_control_test
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where test_id = #{testId} and deleted = 0
        """)
    int softDelete(@Param("testId") Long testId, @Param("updatedBy") Integer updatedBy);

    @Select("""
        select result_status as name, count(1) as total
        from soc_control_test
        where project_id = #{projectId} and deleted = 0
        group by result_status
        """)
    List<Map<String, Object>> countByResultStatus(@Param("projectId") Long projectId);

    @Select("""
        select test_id, project_id, title, description, risk_level, risk_description, coso_principle, control_procedure,
               result_status, current_version, deleted, created_by, updated_by, created_at, updated_at
        from soc_control_test
        where project_id = #{projectId} and deleted = 0
        """)
    List<ControlTest> listByProjectId(@Param("projectId") Long projectId);
}
