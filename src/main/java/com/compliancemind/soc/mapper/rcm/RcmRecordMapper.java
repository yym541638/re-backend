package com.compliancemind.soc.mapper.rcm;

import com.compliancemind.soc.dto.rcm.RcmQueryRequest;
import com.compliancemind.soc.entity.rcm.RcmRecord;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** {@code soc_rcm} RCM 记录。 */
@Mapper
public interface RcmRecordMapper {

    @Select({
        "<script>",
        "select rcm_id, project_id, control_code, control_name, description, category, module_name, risk_description,",
        "status, stage, ai_generated, source_request_id, source_rcm_id, control_objective, implementation_method, evidence_requirement, control_performer,",
        "control_reviewer, additional_owner, control_risk_rating, current_version, deleted, created_by, updated_by, created_at, updated_at",
        "from soc_rcm",
        "where deleted = 0",
        "<if test='query.projectId != null'>",
        "  and project_id = #{query.projectId}",
        "</if>",
        "<if test='query.status != null and query.status != \"\"'>",
        "  and status = #{query.status}",
        "</if>",
        "<if test='query.category != null and query.category != \"\"'>",
        "  and category = #{query.category}",
        "</if>",
        "<if test='query.riskRating != null and query.riskRating != \"\"'>",
        "  and control_risk_rating = #{query.riskRating}",
        "</if>",
        "<if test='query.stage != null and query.stage != \"\"'>",
        "  and stage = #{query.stage}",
        "</if>",
        "<if test='query.sourceRequestId != null'>",
        "  and source_request_id = #{query.sourceRequestId}",
        "</if>",
        "order by updated_at desc",
        "</script>"
    })
    List<RcmRecord> list(@Param("query") RcmQueryRequest query);

    @Select({
        "<script>",
        "select count(1) from soc_rcm where deleted = 0",
        "<if test='query.projectId != null'>",
        "  and project_id = #{query.projectId}",
        "</if>",
        "<if test='query.status != null and query.status != \"\"'>",
        "  and status = #{query.status}",
        "</if>",
        "<if test='query.category != null and query.category != \"\"'>",
        "  and category = #{query.category}",
        "</if>",
        "<if test='query.riskRating != null and query.riskRating != \"\"'>",
        "  and control_risk_rating = #{query.riskRating}",
        "</if>",
        "<if test='query.stage != null and query.stage != \"\"'>",
        "  and stage = #{query.stage}",
        "</if>",
        "<if test='query.sourceRequestId != null'>",
        "  and source_request_id = #{query.sourceRequestId}",
        "</if>",
        "</script>"
    })
    long count(@Param("query") RcmQueryRequest query);

    @Select("""
        select rcm_id, project_id, control_code, control_name, description, category, module_name, risk_description,
               status, stage, ai_generated, source_request_id, source_rcm_id, control_objective, implementation_method, evidence_requirement, control_performer,
               control_reviewer, additional_owner, control_risk_rating, current_version, deleted, created_by, updated_by, created_at, updated_at
        from soc_rcm
        where rcm_id = #{rcmId} and deleted = 0
        """)
    RcmRecord selectById(@Param("rcmId") Long rcmId);

    @Select("""
        select rcm_id, project_id, control_code, control_name, description, category, module_name, risk_description,
               status, stage, ai_generated, source_request_id, source_rcm_id, control_objective, implementation_method, evidence_requirement, control_performer,
               control_reviewer, additional_owner, control_risk_rating, current_version, deleted, created_by, updated_by, created_at, updated_at
        from soc_rcm
        where project_id = #{projectId} and deleted = 0
        order by updated_at desc, rcm_id desc
        """)
    List<RcmRecord> listByProjectId(@Param("projectId") Long projectId);

    @Select("""
        select rcm_id, project_id, control_code, control_name, description, category, module_name, risk_description,
               status, stage, ai_generated, source_request_id, source_rcm_id, control_objective, implementation_method, evidence_requirement, control_performer,
               control_reviewer, additional_owner, control_risk_rating, current_version, deleted, created_by, updated_by, created_at, updated_at
        from soc_rcm
        where project_id = #{projectId}
          and source_request_id = #{sourceRequestId}
          and stage = #{stage}
          and deleted = 0
        limit 1
        """)
    RcmRecord selectBySourceRequestAndStage(@Param("projectId") Long projectId,
                                            @Param("sourceRequestId") Long sourceRequestId,
                                            @Param("stage") String stage);

    @Select("""
        select rcm_id, project_id, control_code, control_name, description, category, module_name, risk_description,
               status, stage, ai_generated, source_request_id, source_rcm_id, control_objective, implementation_method, evidence_requirement, control_performer,
               control_reviewer, additional_owner, control_risk_rating, current_version, deleted, created_by, updated_by, created_at, updated_at
        from soc_rcm
        where project_id = #{projectId}
          and source_rcm_id = #{sourceRcmId}
          and stage = #{stage}
          and deleted = 0
        limit 1
        """)
    RcmRecord selectBySourceRcmAndStage(@Param("projectId") Long projectId,
                                        @Param("sourceRcmId") Long sourceRcmId,
                                        @Param("stage") String stage);

    @Insert("""
        insert into soc_rcm(project_id, control_code, control_name, description, category, module_name, risk_description,
                            status, stage, ai_generated, source_request_id, source_rcm_id, control_objective, implementation_method, evidence_requirement,
                            control_performer, control_reviewer, additional_owner, control_risk_rating, current_version,
                            deleted, created_by, updated_by, created_at, updated_at)
        values(#{projectId}, #{controlCode}, #{controlName}, #{description}, #{category}, #{moduleName}, #{riskDescription},
               #{status}, #{stage}, #{aiGenerated}, #{sourceRequestId}, #{sourceRcmId}, #{controlObjective}, #{implementationMethod}, #{evidenceRequirement},
               #{controlPerformer}, #{controlReviewer}, #{additionalOwner}, #{controlRiskRating}, #{currentVersion},
               #{deleted}, #{createdBy}, #{updatedBy}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "rcmId")
    int insert(RcmRecord rcmRecord);

    @Update("""
        update soc_rcm
        set control_code = #{controlCode},
            control_name = #{controlName},
            description = #{description},
            category = #{category},
            module_name = #{moduleName},
            risk_description = #{riskDescription},
            status = #{status},
            stage = #{stage},
            ai_generated = #{aiGenerated},
            source_request_id = #{sourceRequestId},
            source_rcm_id = #{sourceRcmId},
            control_objective = #{controlObjective},
            implementation_method = #{implementationMethod},
            evidence_requirement = #{evidenceRequirement},
            control_performer = #{controlPerformer},
            control_reviewer = #{controlReviewer},
            additional_owner = #{additionalOwner},
            control_risk_rating = #{controlRiskRating},
            current_version = #{currentVersion},
            updated_by = #{updatedBy},
            updated_at = now()
        where rcm_id = #{rcmId} and deleted = 0
        """)
    int update(RcmRecord rcmRecord);

    @Update("""
        update soc_rcm
        set deleted = 1, updated_by = #{updatedBy}, updated_at = now()
        where rcm_id = #{rcmId} and deleted = 0
        """)
    int softDelete(@Param("rcmId") Long rcmId, @Param("updatedBy") Integer updatedBy);
}
