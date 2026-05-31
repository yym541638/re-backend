package com.compliancemind.soc.mapper.analysis;

import com.compliancemind.soc.entity.analysis.ReportTask;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/** {@code soc_report_task} 报告生成异步任务。 */
@Mapper
public interface ReportTaskMapper {

    @Insert("""
        insert into soc_report_task(project_id, report_type, format, include_sections_json, language, status, progress, file_path, error_message, created_by, created_at, updated_at)
        values(#{projectId}, #{reportType}, #{format}, #{includeSectionsJson}, #{language}, #{status}, #{progress}, #{filePath}, #{errorMessage}, #{createdBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "taskId")
    int insert(ReportTask reportTask);

    @Select("""
        select task_id, project_id, report_type, format, include_sections_json, language, status, progress, file_path, error_message, created_by, created_at, updated_at
        from soc_report_task
        where task_id = #{taskId}
        """)
    ReportTask selectById(@Param("taskId") Long taskId);

    @Update("""
        update soc_report_task
        set status = #{status},
            progress = #{progress},
            file_path = #{filePath},
            error_message = #{errorMessage},
            updated_at = now()
        where task_id = #{taskId}
        """)
    int updateStatus(ReportTask reportTask);
}
