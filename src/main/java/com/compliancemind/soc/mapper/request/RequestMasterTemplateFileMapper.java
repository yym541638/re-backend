package com.compliancemind.soc.mapper.request;

import com.compliancemind.soc.entity.request.RequestMasterTemplateFile;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RequestMasterTemplateFileMapper {

    @Select("""
        select count(1) from soc_request_master_template_file
        where deleted = 0 and request_master_id = #{requestMasterId}
        """)
    long countByMasterId(@Param("requestMasterId") Long requestMasterId);

    @Select("""
        select template_file_id, request_master_id, file_no, file_name, file_path, relevant_criteria,
               deleted, created_by, updated_by, created_at, updated_at
        from soc_request_master_template_file
        where deleted = 0 and request_master_id = #{requestMasterId}
        order by file_no asc, template_file_id asc
        limit #{offset}, #{pageSize}
        """)
    List<RequestMasterTemplateFile> listByMasterId(@Param("requestMasterId") Long requestMasterId,
                                                   @Param("offset") long offset,
                                                   @Param("pageSize") int pageSize);

    @Select("""
        select template_file_id, request_master_id, file_no, file_name, file_path, relevant_criteria,
               deleted, created_by, updated_by, created_at, updated_at
        from soc_request_master_template_file
        where deleted = 0 and request_master_id = #{requestMasterId}
        order by file_no asc, template_file_id asc
        """)
    List<RequestMasterTemplateFile> listAllByMasterId(@Param("requestMasterId") Long requestMasterId);

    @Select("""
        select template_file_id, request_master_id, file_no, file_name, file_path, relevant_criteria,
               deleted, created_by, updated_by, created_at, updated_at
        from soc_request_master_template_file
        where template_file_id = #{templateFileId} and deleted = 0
        """)
    RequestMasterTemplateFile selectById(@Param("templateFileId") Long templateFileId);

    @Select("""
        select coalesce(max(file_no), 0) from soc_request_master_template_file
        where deleted = 0 and request_master_id = #{requestMasterId}
        """)
    int maxFileNo(@Param("requestMasterId") Long requestMasterId);

    @Insert("""
        insert into soc_request_master_template_file(request_master_id, file_no, file_name, file_path, relevant_criteria,
                                                     deleted, created_by, updated_by, created_at, updated_at)
        values(#{requestMasterId}, #{fileNo}, #{fileName}, #{filePath}, #{relevantCriteria},
               #{deleted}, #{createdBy}, #{updatedBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "templateFileId")
    int insert(RequestMasterTemplateFile file);

    @Update("""
        update soc_request_master_template_file
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where template_file_id = #{templateFileId} and deleted = 0
        """)
    int softDelete(@Param("templateFileId") Long templateFileId, @Param("updatedBy") Integer updatedBy);
}
