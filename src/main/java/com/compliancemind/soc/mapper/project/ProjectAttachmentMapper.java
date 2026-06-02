package com.compliancemind.soc.mapper.project;

import com.compliancemind.soc.entity.project.ProjectAttachment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/** {@code soc_project_attachment} 项目附件。 */
@Mapper
public interface ProjectAttachmentMapper {

    @Insert("""
        insert into soc_project_attachment(project_id, file_name, file_path, file_size, deleted, created_by, created_at)
        values(#{projectId}, #{fileName}, #{filePath}, #{fileSize}, 0, #{createdBy}, now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "attachmentId")
    int insert(ProjectAttachment attachment);

    @Select("""
        select attachment_id, project_id, file_name, file_path, file_size, deleted, created_by, created_at
        from soc_project_attachment
        where project_id = #{projectId} and deleted = 0
        order by attachment_id asc
        """)
    List<ProjectAttachment> listByProjectId(@Param("projectId") Long projectId);
}
