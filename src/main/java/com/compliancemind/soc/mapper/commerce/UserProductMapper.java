package com.compliancemind.soc.mapper.commerce;

import com.compliancemind.soc.entity.commerce.UserProduct;
import com.compliancemind.soc.common.constants.SocConstants;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import java.util.List;

/** {@code sys_user_product} 用户已购订阅。 */
@Mapper
public interface UserProductMapper {

    @Select("""
        select user_product_id, user_id, product_id, product_name, package_id, audit_type, included_features, source_order_no,
               status, start_time, end_time, created_at, updated_at
        from sys_user_product
        where user_id = #{userId}
        order by user_product_id desc
        """)
    List<UserProduct> listByUserId(@Param("userId") Integer userId);

    @Select("select count(1) from sys_user_product where user_id = #{userId} and status = '"
        + SocConstants.UserProduct.STATUS_ACTIVE + "'")
    long countActiveByUserId(@Param("userId") Integer userId);

    @Select("""
        select count(1)
        from sys_user_product up
        inner join sys_user u on u.user_id = up.user_id and u.deleted = 0
        where u.company_id = #{companyId}
          and up.status = 'ACTIVE'
        """)
    long countActiveByCompanyId(@Param("companyId") Integer companyId);

    @Select("""
        select user_product_id, user_id, product_id, product_name, package_id, audit_type, included_features, source_order_no,
               status, start_time, end_time, created_at, updated_at
        from sys_user_product
        where user_id = #{userId} and product_id = #{productId} and audit_type = #{auditType}
        limit 1
        """)
    UserProduct selectOne(@Param("userId") Integer userId,
                          @Param("productId") Integer productId,
                          @Param("auditType") String auditType);

    @Select("""
        select user_product_id, user_id, product_id, product_name, package_id, audit_type, included_features, source_order_no,
               status, start_time, end_time, created_at, updated_at
        from sys_user_product
        where user_id = #{userId} and product_id = #{productId}
        order by user_product_id desc
        limit 1
        """)
    UserProduct selectByUserIdAndProductId(@Param("userId") Integer userId,
                                           @Param("productId") Integer productId);

    @Select("""
        select included_features
        from sys_user_product up
        inner join sys_user u on u.user_id = up.user_id
        where u.company_id = #{companyId}
          and up.status = 'ACTIVE'
          and up.included_features is not null
          and up.included_features <> ''
        """)
    List<String> listActiveIncludedFeaturesByCompanyId(@Param("companyId") Integer companyId);

    @Select("""
        select up.user_product_id, up.user_id, up.product_id, up.product_name, up.package_id, up.audit_type,
               up.included_features, up.source_order_no, up.status, up.start_time, up.end_time,
               up.created_at, up.updated_at
        from sys_user_product up
        inner join sys_user u on u.user_id = up.user_id
        where u.company_id = #{companyId}
          and up.status = 'ACTIVE'
        order by up.user_product_id asc
        """)
    List<UserProduct> listActiveByCompanyId(@Param("companyId") Integer companyId);

    @Insert("""
        insert into sys_user_product(user_id, product_id, product_name, package_id, audit_type, included_features, source_order_no,
                                     status, start_time, end_time, created_at, updated_at)
        values(#{userId}, #{productId}, #{productName}, #{packageId}, #{auditType}, #{includedFeatures}, #{sourceOrderNo},
               #{status}, #{startTime}, #{endTime}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "userProductId")
    int insert(UserProduct userProduct);

    @Update("""
        update sys_user_product
        set package_id = #{packageId},
            audit_type = #{auditType},
            included_features = #{includedFeatures},
            source_order_no = #{sourceOrderNo},
            status = #{status},
            start_time = #{startTime},
            end_time = #{endTime},
            updated_at = now()
        where user_product_id = #{userProductId}
        """)
    int update(UserProduct userProduct);

    @Update("""
        update sys_user_product
        set included_features = #{includedFeatures},
            updated_at = now()
        where user_product_id = #{userProductId}
        """)
    int updateIncludedFeatures(UserProduct userProduct);
}
