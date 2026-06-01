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
