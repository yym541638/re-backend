package com.compliancemind.soc.mapper.analysis;

import com.compliancemind.soc.dto.analysis.GapAnalysisQueryRequest;
import com.compliancemind.soc.entity.analysis.GapAnalysisRecord;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** {@code soc_gap_analysis} 差距分析行。 */
@Mapper
public interface GapAnalysisMapper {

    @Select({
        "<script>",
        "select gap_id, project_id, source_test_id, control_title, gap_level, status, gap_description, remediation_suggestion, created_at, updated_at",
        "from soc_gap_analysis",
        "where project_id = #{query.projectId}",
        "<if test='query.gapLevel != null and query.gapLevel != \"\"'>",
        "  and gap_level = #{query.gapLevel}",
        "</if>",
        "<if test='query.status != null and query.status != \"\"'>",
        "  and status = #{query.status}",
        "</if>",
        "order by gap_id desc",
        "</script>"
    })
    List<GapAnalysisRecord> list(@Param("query") GapAnalysisQueryRequest query);

    @Insert("""
        insert into soc_gap_analysis(project_id, source_test_id, control_title, gap_level, status, gap_description, remediation_suggestion, created_at, updated_at)
        values(#{projectId}, #{sourceTestId}, #{controlTitle}, #{gapLevel}, #{status}, #{gapDescription}, #{remediationSuggestion}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "gapId")
    int insert(GapAnalysisRecord record);

    @Delete("delete from soc_gap_analysis where project_id = #{projectId}")
    int deleteByProjectId(@Param("projectId") Long projectId);

    @Select("select count(1) from soc_gap_analysis where project_id = #{projectId}")
    long countByProjectId(@Param("projectId") Long projectId);
}

