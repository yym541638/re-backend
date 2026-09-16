package com.compliancemind.soc.mapper.risk;

import com.compliancemind.soc.dto.risk.RiskQueryRequest;
import com.compliancemind.soc.entity.risk.RiskRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** {@code soc_risk} 项目风险表。 */
@Mapper
public interface RiskMapper {

    @Select({
        "<script>",
        "select count(1) from soc_risk",
        "where deleted = 0 and project_id = #{query.projectId}",
        "<if test='query.ccCriteria != null and query.ccCriteria != \"\"'>",
        "  and cc_criteria = #{query.ccCriteria}",
        "</if>",
        "<if test='query.riskLevel != null and query.riskLevel != \"\"'>",
        "  and risk_level = #{query.riskLevel}",
        "</if>",
        "<if test='query.riskSource != null and query.riskSource != \"\"'>",
        "  and risk_source = #{query.riskSource}",
        "</if>",
        "<if test='query.keyword != null and query.keyword != \"\"'>",
        "  and (",
        "    cc_criteria_name like concat('%', #{query.keyword}, '%')",
        "    or sub_risk_name like concat('%', #{query.keyword}, '%')",
        "    or points_of_focus_name like concat('%', #{query.keyword}, '%')",
        "    or additional_risk_profile_description like concat('%', #{query.keyword}, '%')",
        "  )",
        "</if>",
        "</script>"
    })
    long count(@Param("query") RiskQueryRequest query);

    @Select({
        "<script>",
        "select risk_id, project_id, cc_criteria, cycle_name, modules_id, modules_name, cc_criteria_name,",
        "sub_risk_id, sub_risk_name, points_of_focus_id, points_of_focus_name, risk_level, risk_source,",
        "additional_risk_profile_description, deleted, created_by, updated_by, created_at, updated_at",
        "from soc_risk",
        "where deleted = 0 and project_id = #{query.projectId}",
        "<if test='query.ccCriteria != null and query.ccCriteria != \"\"'>",
        "  and cc_criteria = #{query.ccCriteria}",
        "</if>",
        "<if test='query.riskLevel != null and query.riskLevel != \"\"'>",
        "  and risk_level = #{query.riskLevel}",
        "</if>",
        "<if test='query.riskSource != null and query.riskSource != \"\"'>",
        "  and risk_source = #{query.riskSource}",
        "</if>",
        "<if test='query.keyword != null and query.keyword != \"\"'>",
        "  and (",
        "    cc_criteria_name like concat('%', #{query.keyword}, '%')",
        "    or sub_risk_name like concat('%', #{query.keyword}, '%')",
        "    or points_of_focus_name like concat('%', #{query.keyword}, '%')",
        "    or additional_risk_profile_description like concat('%', #{query.keyword}, '%')",
        "  )",
        "</if>",
        "order by risk_id asc",
        "limit #{offset}, #{pageSize}",
        "</script>"
    })
    List<RiskRecord> list(@Param("query") RiskQueryRequest query,
                          @Param("offset") long offset,
                          @Param("pageSize") int pageSize);

    @Select("""
        select risk_id, project_id, cc_criteria, cycle_name, modules_id, modules_name, cc_criteria_name,
               sub_risk_id, sub_risk_name, points_of_focus_id, points_of_focus_name, risk_level, risk_source,
               additional_risk_profile_description, deleted, created_by, updated_by, created_at, updated_at
        from soc_risk
        where risk_id = #{riskId} and deleted = 0
        """)
    RiskRecord selectById(@Param("riskId") Long riskId);

    @Select("""
        select count(1) from soc_risk
        where deleted = 0 and project_id = #{projectId}
        """)
    long countByProjectId(@Param("projectId") Long projectId);

    @Select("""
        select count(1) from soc_risk
        where deleted = 0
          and project_id = #{projectId}
          and cc_criteria = #{ccCriteria}
          and ifnull(points_of_focus_name, '') = ifnull(#{pointsOfFocusName}, '')
        """)
    long countByProjectCriteriaAndFocus(@Param("projectId") Long projectId,
                                        @Param("ccCriteria") String ccCriteria,
                                        @Param("pointsOfFocusName") String pointsOfFocusName);

    @Insert("""
        insert into soc_risk(
            project_id, cc_criteria, cycle_name, modules_id, modules_name, cc_criteria_name,
            sub_risk_id, sub_risk_name, points_of_focus_id, points_of_focus_name, risk_level, risk_source,
            additional_risk_profile_description, deleted, created_by, updated_by, created_at, updated_at
        ) values(
            #{projectId}, #{ccCriteria}, #{cycleName}, #{modulesId}, #{modulesName}, #{ccCriteriaName},
            #{subRiskId}, #{subRiskName}, #{pointsOfFocusId}, #{pointsOfFocusName}, #{riskLevel}, #{riskSource},
            #{additionalRiskProfileDescription}, #{deleted}, #{createdBy}, #{updatedBy}, now(), now()
        )
        """)
    @Options(useGeneratedKeys = true, keyProperty = "riskId")
    int insert(RiskRecord record);

    @Update("""
        update soc_risk
        set cc_criteria = #{ccCriteria},
            cycle_name = #{cycleName},
            modules_id = #{modulesId},
            modules_name = #{modulesName},
            cc_criteria_name = #{ccCriteriaName},
            sub_risk_id = #{subRiskId},
            sub_risk_name = #{subRiskName},
            points_of_focus_id = #{pointsOfFocusId},
            points_of_focus_name = #{pointsOfFocusName},
            risk_level = #{riskLevel},
            risk_source = #{riskSource},
            additional_risk_profile_description = #{additionalRiskProfileDescription},
            updated_by = #{updatedBy},
            updated_at = now()
        where risk_id = #{riskId} and deleted = 0
        """)
    int update(RiskRecord record);

    @Update("""
        update soc_risk
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where risk_id = #{riskId} and deleted = 0
        """)
    int softDelete(@Param("riskId") Long riskId, @Param("updatedBy") Integer updatedBy);
}
