package com.compliancemind.soc.mapper.request;

import com.compliancemind.soc.entity.request.RequestMaster;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** {@code soc_request_master}。 */
@Mapper
public interface RequestMasterMapper {

    @Select("""
        select request_master_id, project_id, request_master_code, request_master_name, status, current_version_id,
               deleted, created_by, updated_by, created_at, updated_at
        from soc_request_master
        where deleted = 0 and project_id = #{projectId}
        order by created_at desc
        """)
    List<RequestMaster> listByProjectId(@Param("projectId") Long projectId);

    @Select("""
        select request_master_id, project_id, request_master_code, request_master_name, status, current_version_id,
               deleted, created_by, updated_by, created_at, updated_at
        from soc_request_master
        where request_master_id = #{requestMasterId} and deleted = 0
        """)
    RequestMaster selectById(@Param("requestMasterId") Long requestMasterId);

    @Insert("""
        insert into soc_request_master(project_id, request_master_code, request_master_name, status,
                                       deleted, created_by, updated_by, created_at, updated_at)
        values(#{projectId}, #{requestMasterCode}, #{requestMasterName}, #{status},
               #{deleted}, #{createdBy}, #{updatedBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "requestMasterId")
    int insert(RequestMaster requestMaster);

    @Update("""
        update soc_request_master
        set request_master_code = #{requestMasterCode}
        where request_master_id = #{requestMasterId} and deleted = 0
        """)
    int updateCode(RequestMaster requestMaster);

    @Update("""
        update soc_request_master
        set request_master_name = #{requestMasterName},
            status = #{status},
            updated_by = #{updatedBy},
            updated_at = now()
        where request_master_id = #{requestMasterId} and deleted = 0
        """)
    int update(RequestMaster requestMaster);

    @Update("""
        update soc_request_master
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where request_master_id = #{requestMasterId} and deleted = 0
        """)
    int softDelete(@Param("requestMasterId") Long requestMasterId, @Param("updatedBy") Integer updatedBy);

    @Update("""
        update soc_request_master
        set current_version_id = #{versionId}, updated_by = #{updatedBy}, updated_at = now()
        where request_master_id = #{requestMasterId} and deleted = 0
        """)
    int updateCurrentVersion(@Param("requestMasterId") Long requestMasterId,
                             @Param("versionId") Long versionId,
                             @Param("updatedBy") Integer updatedBy);
}
