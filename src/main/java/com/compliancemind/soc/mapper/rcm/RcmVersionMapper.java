package com.compliancemind.soc.mapper.rcm;

import com.compliancemind.soc.entity.rcm.RcmVersion;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** {@code soc_rcm_version} RCM 版本快照。 */
@Mapper
public interface RcmVersionMapper {

    @Select("""
        select version_id, rcm_id, version_no, snapshot_json, change_summary, created_by, created_at
        from soc_rcm_version
        where rcm_id = #{rcmId}
        order by version_id desc
        """)
    List<RcmVersion> listByRcmId(@Param("rcmId") Long rcmId);

    @Insert("""
        insert into soc_rcm_version(rcm_id, version_no, snapshot_json, change_summary, created_by, created_at)
        values(#{rcmId}, #{versionNo}, #{snapshotJson}, #{changeSummary}, #{createdBy}, now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "versionId")
    int insert(RcmVersion version);
}
