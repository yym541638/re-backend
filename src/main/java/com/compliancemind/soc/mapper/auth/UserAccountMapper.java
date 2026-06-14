package com.compliancemind.soc.mapper.auth;

import com.compliancemind.soc.entity.auth.UserAccount;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** {@code sys_user} 用户账号。 */
@Mapper
public interface UserAccountMapper {

    @Select("""
        select user_id, company_id, display_name, email, phone, avatar_url, job_title, password_hash, role_code, status, created_at, updated_at
        from sys_user
        where (email = #{account} or phone = #{account}) and deleted = 0
        limit 1
        """)
    UserAccount selectByAccount(@Param("account") String account);

    @Select("""
        select user_id, company_id, display_name, email, phone, avatar_url, job_title, password_hash, role_code, status, created_at, updated_at
        from sys_user
        where user_id = #{userId} and deleted = 0
        """)
    UserAccount selectById(@Param("userId") Integer userId);

    @Select("""
        select user_id, company_id, display_name, email, phone, avatar_url, job_title, password_hash, role_code, status, created_at, updated_at
        from sys_user
        where user_id = #{userId} and company_id = #{companyId} and deleted = 0
        """)
    UserAccount selectByIdAndCompanyId(@Param("userId") Integer userId, @Param("companyId") Integer companyId);

    @Select("select count(1) from sys_user where email = #{email} and deleted = 0")
    long countByEmail(@Param("email") String email);

    @Select("select count(1) from sys_user where phone = #{phone} and deleted = 0")
    long countByPhone(@Param("phone") String phone);

    @Select("""
        select count(1) from sys_user
        where company_id = #{companyId} and role_code = #{roleCode} and deleted = 0
        """)
    long countByCompanyIdAndRoleCode(@Param("companyId") Integer companyId,
                                     @Param("roleCode") String roleCode);

    @Select("""
        <script>
        select user_id, company_id, display_name, email, phone, avatar_url, job_title, password_hash, role_code, status, created_at, updated_at
        from sys_user
        where deleted = 0 and company_id = #{companyId}
        <if test='keyword != null and keyword != ""'>
          and (display_name like concat('%', #{keyword}, '%') or email like concat('%', #{keyword}, '%') or phone like concat('%', #{keyword}, '%'))
        </if>
        order by user_id desc
        </script>
        """)
    java.util.List<UserAccount> listUsers(@Param("companyId") Integer companyId,
                                          @Param("keyword") String keyword);

    @Insert("""
        insert into sys_user(company_id, display_name, email, phone, avatar_url, job_title, password_hash, role_code, status, deleted, created_at, updated_at)
        values(#{companyId}, #{displayName}, #{email}, #{phone}, #{avatarUrl}, #{jobTitle}, #{passwordHash}, #{roleCode}, #{status}, 0, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "userId")
    int insert(UserAccount userAccount);

    @org.apache.ibatis.annotations.Update("""
        update sys_user
        set display_name = #{displayName},
            email = #{email},
            phone = #{phone},
            avatar_url = #{avatarUrl},
            job_title = #{jobTitle},
            updated_at = now()
        where user_id = #{userId} and deleted = 0
        """)
    int updateProfile(UserAccount userAccount);
}
