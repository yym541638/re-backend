package com.compliancemind.soc.mapper.commerce;

import com.compliancemind.soc.entity.commerce.OrderRecord;
import com.compliancemind.soc.common.constants.SocConstants;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.time.LocalDateTime;
import java.util.List;

/** {@code sys_order} 订单。 */
@Mapper
public interface OrderMapper {

    @Insert("""
        insert into sys_order(order_no, user_id, product_id, package_id, product_name, package_name, audit_type, included_features, amount,
                              payment_method, status, transaction_id, return_url, notify_url, pay_time, created_at, updated_at)
        values(#{orderNo}, #{userId}, #{productId}, #{packageId}, #{productName}, #{packageName}, #{auditType}, #{includedFeatures}, #{amount},
               #{paymentMethod}, #{status}, #{transactionId}, #{returnUrl}, #{notifyUrl}, #{payTime}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "orderId")
    int insert(OrderRecord orderRecord);

    @Select("""
        select order_id, order_no, user_id, product_id, package_id, product_name, package_name, audit_type, included_features, amount,
               payment_method, status, transaction_id, return_url, notify_url, pay_time, created_at, updated_at
        from sys_order
        where order_no = #{orderNo}
        """)
    OrderRecord selectByOrderNo(@Param("orderNo") String orderNo);

    @Select("""
        select order_id, order_no, user_id, product_id, package_id, product_name, package_name, audit_type, included_features, amount,
               payment_method, status, transaction_id, return_url, notify_url, pay_time, created_at, updated_at
        from sys_order
        where user_id = #{userId}
        order by order_id desc
        """)
    List<OrderRecord> listAllByUserId(@Param("userId") Integer userId);

    @Select("select count(1) from sys_order where user_id = #{userId} and status = '" + SocConstants.Order.STATUS_PAID + "'")
    long countPaidByUserId(@Param("userId") Integer userId);

    @Update("""
        update sys_order
        set status = #{status},
            transaction_id = #{transactionId},
            pay_time = #{payTime},
            updated_at = now()
        where order_no = #{orderNo}
        """)
    int updatePayment(@Param("orderNo") String orderNo,
                      @Param("status") String status,
                      @Param("transactionId") String transactionId,
                      @Param("payTime") LocalDateTime payTime);

    @Update("update sys_order set status = '" + SocConstants.Order.STATUS_CANCELED + "', updated_at = now() "
        + "where order_no = #{orderNo} and user_id = #{userId} and status = '" + SocConstants.Order.STATUS_PENDING + "'")
    int cancel(@Param("userId") Integer userId, @Param("orderNo") String orderNo);
}
