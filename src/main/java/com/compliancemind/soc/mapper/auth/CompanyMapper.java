package com.compliancemind.soc.mapper.auth;

import com.compliancemind.soc.entity.auth.Company;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/** {@code sys_company} 公司主体。 */
@Mapper
public interface CompanyMapper {

    @Select("""
        select company_id, company_name, company_code, industry, website, contact_name, contact_phone, address, created_at, updated_at
        from sys_company
        where company_name = #{companyName}
        limit 1
        """)
    Company selectByName(@Param("companyName") String companyName);

    @Select("""
        select company_id, company_name, company_code, industry, website, contact_name, contact_phone, address, created_at, updated_at
        from sys_company
        where company_id = #{companyId}
        """)
    Company selectById(@Param("companyId") Integer companyId);

    @Insert("""
        insert into sys_company(company_name, created_at, updated_at)
        values(#{companyName}, now(), now())
        """)
    @Options(useGeneratedKeys = true, keyProperty = "companyId")
    int insert(Company company);

    @org.apache.ibatis.annotations.Update("""
        update sys_company
        set company_name = #{companyName},
            company_code = #{companyCode},
            industry = #{industry},
            website = #{website},
            contact_name = #{contactName},
            contact_phone = #{contactPhone},
            address = #{address},
            updated_at = now()
        where company_id = #{companyId}
        """)
    int update(Company company);
}
