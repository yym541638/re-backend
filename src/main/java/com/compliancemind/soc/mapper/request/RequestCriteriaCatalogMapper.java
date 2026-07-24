package com.compliancemind.soc.mapper.request;

import com.compliancemind.soc.entity.request.RequestCriteriaCatalog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Collection;
import java.util.List;

/** {@code soc_request_criteria_catalog} 标准条款目录。 */
@Mapper
public interface RequestCriteriaCatalogMapper {

    @Select("""
        <script>
        select catalog_id, criteria_code, module_name, requirement, points_of_focus, document_description,
               sort_order, deleted, created_at, updated_at
        from soc_request_criteria_catalog
        where deleted = 0
          and module_name in
          <foreach collection="moduleNames" item="moduleName" open="(" separator="," close=")">
            #{moduleName}
          </foreach>
        order by sort_order asc, catalog_id asc
        </script>
        """)
    List<RequestCriteriaCatalog> listByModuleNames(@Param("moduleNames") Collection<String> moduleNames);

    @Select("""
        select catalog_id, criteria_code, module_name, requirement, points_of_focus, document_description,
               sort_order, deleted, created_at, updated_at
        from soc_request_criteria_catalog
        where catalog_id = #{catalogId} and deleted = 0
        """)
    RequestCriteriaCatalog selectById(@Param("catalogId") Long catalogId);
}
