package com.compliancemind.soc.mapper.request;

import com.compliancemind.soc.entity.request.RequestAttachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** {@code soc_request_attachment} 请求附件。 */
@Mapper
public interface RequestAttachmentMapper {

    @Select("""
        select attachment_id, request_id, file_name, file_path, file_type, content_type, file_size,
               deleted, created_by, updated_by, created_at, updated_at
        from soc_request_attachment
        where request_id = #{requestId} and deleted = 0
        order by attachment_id desc
        """)
    List<RequestAttachment> listByRequestId(@Param("requestId") Long requestId);

    @Select("""
        select attachment_id, request_id, file_name, file_path, file_type, content_type, file_size,
               deleted, created_by, updated_by, created_at, updated_at
        from soc_request_attachment
        where attachment_id = #{attachmentId} and deleted = 0
        """)
    RequestAttachment selectById(@Param("attachmentId") Long attachmentId);

    @Insert("""
        insert into soc_request_attachment(request_id, file_name, file_path, file_type, content_type, file_size,
                                           deleted, created_by, updated_by, created_at, updated_at)
        values(#{requestId}, #{fileName}, #{filePath}, #{fileType}, #{contentType}, #{fileSize},
               #{deleted}, #{createdBy}, #{updatedBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "attachmentId")
    int insert(RequestAttachment attachment);

    @Update("""
        update soc_request_attachment
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where attachment_id = #{attachmentId} and deleted = 0
        """)
    int softDelete(@Param("attachmentId") Long attachmentId, @Param("updatedBy") Integer updatedBy);
}
