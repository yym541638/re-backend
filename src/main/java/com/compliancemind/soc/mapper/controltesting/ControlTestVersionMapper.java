package com.compliancemind.soc.mapper.controltesting;

import com.compliancemind.soc.entity.controltesting.ControlTestVersion;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** {@code soc_control_test_version} 控制测试版本快照。 */
@Mapper
public interface ControlTestVersionMapper {

    @Select("""
        select version_id, test_id, version_no, snapshot_json, change_summary, created_by, created_at
        from soc_control_test_version
        where test_id = #{testId}
        order by version_id desc
        """)
    List<ControlTestVersion> listByTestId(@Param("testId") Long testId);

    @Insert("""
        insert into soc_control_test_version(test_id, version_no, snapshot_json, change_summary, created_by, created_at)
        values(#{testId}, #{versionNo}, #{snapshotJson}, #{changeSummary}, #{createdBy}, now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "versionId")
    int insert(ControlTestVersion version);
}
