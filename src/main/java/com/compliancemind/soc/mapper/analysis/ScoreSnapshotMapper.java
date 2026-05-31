package com.compliancemind.soc.mapper.analysis;

import com.compliancemind.soc.entity.analysis.ScoreSnapshot;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** {@code soc_score_snapshot} 通过率等指标快照。 */
@Mapper
public interface ScoreSnapshotMapper {

    @Insert("""
        insert into soc_score_snapshot(project_id, snapshot_date, total_count, passed_count, failed_count, pending_count, gap_count, pass_rate, assessment, created_at)
        values(#{projectId}, #{snapshotDate}, #{totalCount}, #{passedCount}, #{failedCount}, #{pendingCount}, #{gapCount}, #{passRate}, #{assessment}, now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "snapshotId")
    int insert(ScoreSnapshot snapshot);

    @Select("""
        select snapshot_id, project_id, snapshot_date, total_count, passed_count, failed_count, pending_count, gap_count, pass_rate, assessment, created_at
        from soc_score_snapshot
        where project_id = #{projectId}
        order by snapshot_id desc
        limit 1
        """)
    ScoreSnapshot selectLatest(@Param("projectId") Long projectId);

    @Select("""
        select snapshot_id, project_id, snapshot_date, total_count, passed_count, failed_count, pending_count, gap_count, pass_rate, assessment, created_at
        from soc_score_snapshot
        where project_id = #{projectId}
        order by snapshot_date asc, snapshot_id asc
        """)
    List<ScoreSnapshot> listByProjectId(@Param("projectId") Long projectId);
}
