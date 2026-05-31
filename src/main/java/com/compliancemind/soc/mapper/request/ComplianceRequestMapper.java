package com.compliancemind.soc.mapper.request;

import com.compliancemind.soc.dto.request.RequestQueryRequest;
import com.compliancemind.soc.entity.request.ComplianceRequest;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** {@code soc_request} 合规请求主表。 */
@Mapper
public interface ComplianceRequestMapper {

    @Select({
        "<script>",
        "select request_id, project_id, request_code, cc_criteria, title, request_description, points_of_focus,",
        "document_status, document_owner, implementation_date, last_update_at, notes, requestor, comments,",
        "current_version, deleted, created_by, updated_by, created_at, updated_at",
        "from soc_request",
        "where deleted = 0",
        "<if test='query.projectId != null'>",
        "  and project_id = #{query.projectId}",
        "</if>",
        "<if test='query.documentStatus != null and query.documentStatus != \"\"'>",
        "  and document_status = #{query.documentStatus}",
        "</if>",
        "<if test='query.ccCriteria != null and query.ccCriteria != \"\"'>",
        "  and cc_criteria = #{query.ccCriteria}",
        "</if>",
        "order by updated_at desc",
        "</script>"
    })
    List<ComplianceRequest> listAll(@Param("query") RequestQueryRequest query);

    @Select("""
        select request_id, project_id, request_code, cc_criteria, title, request_description, points_of_focus,
               document_status, document_owner, implementation_date, last_update_at, notes, requestor, comments,
               current_version, deleted, created_by, updated_by, created_at, updated_at
        from soc_request
        where request_id = #{requestId} and deleted = 0
        """)
    ComplianceRequest selectById(@Param("requestId") Long requestId);

    @Insert("""
        insert into soc_request(project_id, request_code, cc_criteria, title, request_description, points_of_focus,
                                document_status, document_owner, implementation_date, last_update_at, notes, requestor,
                                comments, current_version, deleted, created_by, updated_by, created_at, updated_at)
        values(#{projectId}, #{requestCode}, #{ccCriteria}, #{title}, #{requestDescription}, #{pointsOfFocus},
               #{documentStatus}, #{documentOwner}, #{implementationDate}, #{lastUpdateAt}, #{notes}, #{requestor},
               #{comments}, #{currentVersion}, #{deleted}, #{createdBy}, #{updatedBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "requestId")
    int insert(ComplianceRequest request);

    @Update("""
        update soc_request
        set cc_criteria = #{ccCriteria},
            title = #{title},
            request_description = #{requestDescription},
            points_of_focus = #{pointsOfFocus},
            document_status = #{documentStatus},
            document_owner = #{documentOwner},
            implementation_date = #{implementationDate},
            last_update_at = #{lastUpdateAt},
            notes = #{notes},
            requestor = #{requestor},
            comments = #{comments},
            current_version = #{currentVersion},
            updated_by = #{updatedBy},
            updated_at = now()
        where request_id = #{requestId} and deleted = 0
        """)
    int update(ComplianceRequest request);

    @Update("""
        update soc_request
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where request_id = #{requestId} and deleted = 0
        """)
    int softDelete(@Param("requestId") Long requestId, @Param("updatedBy") Integer updatedBy);
}
