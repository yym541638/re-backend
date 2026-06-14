package com.compliancemind.soc.mapper.request;

import com.compliancemind.soc.entity.request.RequestMasterVersion;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RequestMasterVersionMapper {

    @Select("""
        select version_id, request_master_id, version_label, snapshot_json, is_latest, created_by, created_at
        from soc_request_master_version
        where request_master_id = #{requestMasterId}
        order by version_id desc
        """)
    List<RequestMasterVersion> listByMasterId(@Param("requestMasterId") Long requestMasterId);

    @Select("""
        select version_id, request_master_id, version_label, snapshot_json, is_latest, created_by, created_at
        from soc_request_master_version
        where version_id = #{versionId}
        """)
    RequestMasterVersion selectById(@Param("versionId") Long versionId);

    @Insert("""
        insert into soc_request_master_version(request_master_id, version_label, snapshot_json, is_latest, created_by, created_at)
        values(#{requestMasterId}, #{versionLabel}, #{snapshotJson}, #{isLatest}, #{createdBy}, now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "versionId")
    int insert(RequestMasterVersion version);

    @Update("""
        update soc_request_master_version
        set is_latest = 0
        where request_master_id = #{requestMasterId}
        """)
    int clearLatest(@Param("requestMasterId") Long requestMasterId);
}
