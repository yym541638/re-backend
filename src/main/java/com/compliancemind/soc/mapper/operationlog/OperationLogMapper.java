package com.compliancemind.soc.mapper.operationlog;

import com.compliancemind.soc.dto.operationlog.OperationLogQueryRequest;
import com.compliancemind.soc.entity.operationlog.OperationLog;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/** {@code soc_operation_log} 操作审计日志。 */
@Mapper
public interface OperationLogMapper {

    @Select({
        "<script>",
        "select log_id, user_id, username, module_name, action_type, resource_type, resource_id, resource_name, project_id, action_detail, created_at",
        "from soc_operation_log",
        "where 1 = 1",
        "<if test='query.moduleName != null and query.moduleName != \"\"'>",
        "  and module_name = #{query.moduleName}",
        "</if>",
        "<if test='query.actionType != null and query.actionType != \"\"'>",
        "  and action_type = #{query.actionType}",
        "</if>",
        "<if test='query.projectId != null'>",
        "  and project_id = #{query.projectId}",
        "</if>",
        "order by log_id desc",
        "</script>"
    })
    List<OperationLog> list(@Param("query") OperationLogQueryRequest query);

    @Select({
        "<script>",
        "select count(1) from soc_operation_log",
        "where 1 = 1",
        "<if test='query.moduleName != null and query.moduleName != \"\"'>",
        "  and module_name = #{query.moduleName}",
        "</if>",
        "<if test='query.actionType != null and query.actionType != \"\"'>",
        "  and action_type = #{query.actionType}",
        "</if>",
        "<if test='query.projectId != null'>",
        "  and project_id = #{query.projectId}",
        "</if>",
        "</script>"
    })
    long count(@Param("query") OperationLogQueryRequest query);

    @Insert("""
        insert into soc_operation_log(user_id, username, module_name, action_type, resource_type, resource_id, resource_name, project_id, action_detail, created_at)
        values(#{userId}, #{username}, #{moduleName}, #{actionType}, #{resourceType}, #{resourceId}, #{resourceName}, #{projectId}, #{actionDetail}, now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "logId")
    int insert(OperationLog operationLog);

    @Select("""
        select module_name as name, count(1) as total
        from soc_operation_log
        where project_id = #{projectId}
        group by module_name
        """)
    List<Map<String, Object>> countByModule(@Param("projectId") Long projectId);

    @Select("""
        select action_type as name, count(1) as total
        from soc_operation_log
        where project_id = #{projectId}
        group by action_type
        """)
    List<Map<String, Object>> countByActionType(@Param("projectId") Long projectId);
}
