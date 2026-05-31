package com.compliancemind.soc.mapper.request;

import com.compliancemind.soc.entity.request.RequestVersion;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** {@code soc_request_version} 请求版本快照。 */
@Mapper
public interface RequestVersionMapper {

    @Select("""
        select version_id, request_id, version_no, snapshot_json, change_summary, created_by, created_at
        from soc_request_version
        where request_id = #{requestId}
        order by version_id desc
        """)
    List<RequestVersion> listByRequestId(@Param("requestId") Long requestId);

    @Select("select count(1) from soc_request_version where request_id = #{requestId}")
    long countByRequestId(@Param("requestId") Long requestId);

    @Insert("""
        insert into soc_request_version(request_id, version_no, snapshot_json, change_summary, created_by, created_at)
        values(#{requestId}, #{versionNo}, #{snapshotJson}, #{changeSummary}, #{createdBy}, now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "versionId")
    int insert(RequestVersion version);
}

