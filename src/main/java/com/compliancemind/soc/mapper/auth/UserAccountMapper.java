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
        select user_id, company_id, display_name, email, phone, avatar_url, job_title, user_type,
               password_hash, role_code, status, created_at, updated_at
        from sys_user
        where (email = #{account} or phone = #{account}) and deleted = 0
        limit 1
        """)
    UserAccount selectByAccount(@Param("account") String account);

    @Select("""
        select user_id, company_id, display_name, email, phone, avatar_url, job_title, user_type,
               password_hash, role_code, status, created_at, updated_at
        from sys_user
        where user_id = #{userId} and deleted = 0
        """)
    UserAccount selectById(@Param("userId") Integer userId);

    @Select("""
        select user_id, company_id, display_name, email, phone, avatar_url, job_title, user_type,
               password_hash, role_code, status, created_at, updated_at
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
        where email = #{email} and user_id <> #{userId} and deleted = 0
        """)
    long countByEmailExcludeUserId(@Param("email") String email, @Param("userId") Integer userId);

    @Select("""
        select count(1) from sys_user
        where phone = #{phone} and user_id <> #{userId} and deleted = 0
        """)
    long countByPhoneExcludeUserId(@Param("phone") String phone, @Param("userId") Integer userId);

    @Select("""
        select count(1) from sys_user
        where company_id = #{companyId} and role_code = #{roleCode} and deleted = 0
        """)
    long countByCompanyIdAndRoleCode(@Param("companyId") Integer companyId,
                                     @Param("roleCode") String roleCode);

    @Select("""
        <script>
        select user_id, company_id, display_name, email, phone, avatar_url, job_title, user_type,
               password_hash, role_code, status, created_at, updated_at
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

    /**
     * 系统管理员：全部用户（跨公司），附带公司名称。
     */
    @Select("""
        <script>
        select u.user_id, u.company_id, u.display_name, u.email, u.phone, u.avatar_url, u.job_title, u.user_type,
               u.password_hash, u.role_code, u.status, u.created_at, u.updated_at,
               c.company_name as company_name
        from sys_user u
        left join sys_company c on c.company_id = u.company_id
        where u.deleted = 0
        <if test='keyword != null and keyword != ""'>
          and (u.display_name like concat('%', #{keyword}, '%')
            or u.email like concat('%', #{keyword}, '%')
            or u.phone like concat('%', #{keyword}, '%')
            or c.company_name like concat('%', #{keyword}, '%'))
        </if>
        order by u.user_id desc
        </script>
        """)
    java.util.List<UserAccount> listAllUsers(@Param("keyword") String keyword);

    /**
     * 项目管理员：本公司项目下已出现过的成员用户。
     */
    @Select("""
        <script>
        select distinct u.user_id, u.company_id, u.display_name, u.email, u.phone, u.avatar_url, u.job_title, u.user_type,
               u.password_hash, u.role_code, u.status, u.created_at, u.updated_at
        from sys_user u
        inner join soc_project_member m on m.user_id = u.user_id and m.deleted = 0
        inner join soc_project p on p.project_id = m.project_id and p.deleted = 0
        where u.deleted = 0 and p.company_id = #{companyId}
        <if test='keyword != null and keyword != ""'>
          and (u.display_name like concat('%', #{keyword}, '%') or u.email like concat('%', #{keyword}, '%') or u.phone like concat('%', #{keyword}, '%'))
        </if>
        order by u.user_id desc
        </script>
        """)
    java.util.List<UserAccount> listUsersInCompanyProjects(@Param("companyId") Integer companyId,
                                                           @Param("keyword") String keyword);

    @Insert("""
        insert into sys_user(company_id, display_name, email, phone, avatar_url, job_title, user_type,
                             password_hash, role_code, status, deleted, created_at, updated_at)
        values(#{companyId}, #{displayName}, #{email}, #{phone}, #{avatarUrl}, #{jobTitle}, #{userType},
               #{passwordHash}, #{roleCode}, #{status}, 0, now(), now())
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

    @org.apache.ibatis.annotations.Update("""
        update sys_user
        set role_code = #{roleCode},
            updated_at = now()
        where user_id = #{userId} and deleted = 0
        """)
    int updateRoleCodeByUserId(@Param("userId") Integer userId,
                               @Param("roleCode") String roleCode);

    @org.apache.ibatis.annotations.Update("""
        update sys_user
        set role_code = #{roleCode},
            updated_at = now()
        where user_id = #{userId} and company_id = #{companyId} and deleted = 0
        """)
    int updateRoleCode(@Param("userId") Integer userId,
                        @Param("companyId") Integer companyId,
                        @Param("roleCode") String roleCode);
}
