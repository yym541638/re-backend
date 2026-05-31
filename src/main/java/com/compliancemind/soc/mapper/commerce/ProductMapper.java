package com.compliancemind.soc.mapper.commerce;

import com.compliancemind.soc.entity.commerce.Product;
import com.compliancemind.soc.entity.commerce.ProductPackage;
import com.compliancemind.soc.common.constants.SocConstants;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/** 商品与套餐目录及调价。 */
@Mapper
public interface ProductMapper {

    @Select("select product_id, product_name, product_code, introduction_title, introduction_text, logo_url, "
        + "trust_principles, all_features, status, sort_no, created_at, updated_at "
        + "from sys_product "
        + "where status = " + SocConstants.Product.STATUS_ENABLED + " "
        + "order by sort_no asc, product_id asc")
    List<Product> listActiveProducts();

    @Select("select product_id, product_name, product_code, introduction_title, introduction_text, logo_url, "
        + "trust_principles, all_features, status, sort_no, created_at, updated_at "
        + "from sys_product "
        + "where product_id = #{productId} and status = " + SocConstants.Product.STATUS_ENABLED)
    Product selectById(@Param("productId") Integer productId);

    @Select("select product_id, product_name, product_code, introduction_title, introduction_text, logo_url, "
        + "trust_principles, all_features, status, sort_no, created_at, updated_at "
        + "from sys_product "
        + "where product_code = #{productCode} and status = " + SocConstants.Product.STATUS_ENABLED + " "
        + "limit 1")
    Product selectByProductCode(@Param("productCode") String productCode);

    @Select("select package_id, product_id, package_name, annual_price, type1_price, type2_price, "
        + "included_features, supported_types, default_type, "
        + "status, sort_no, created_at, updated_at "
        + "from sys_product_package "
        + "where product_id = #{productId} and status = " + SocConstants.Product.STATUS_ENABLED + " "
        + "order by sort_no asc, package_id asc")
    List<ProductPackage> listPackagesByProductId(@Param("productId") Integer productId);

    @Select("select package_id, product_id, package_name, annual_price, type1_price, type2_price, "
        + "included_features, supported_types, default_type, "
        + "status, sort_no, created_at, updated_at "
        + "from sys_product_package "
        + "where package_id = #{packageId} and status = " + SocConstants.Product.STATUS_ENABLED)
    ProductPackage selectPackageById(@Param("packageId") Integer packageId);

    @Update("update sys_product_package "
        + "set annual_price = CASE "
        + "WHEN #{defaultType} = '" + SocConstants.AuditType.DISPLAY_TYPE2 + "' THEN #{type2Price} "
        + "ELSE #{type1Price} END, "
        + "type1_price = #{type1Price}, "
        + "type2_price = #{type2Price}, "
        + "default_type = #{defaultType}, "
        + "updated_at = now() "
        + "where package_id = #{packageId}")
    int updatePackagePrice(@Param("packageId") Integer packageId,
                           @Param("type1Price") Integer type1Price,
                           @Param("type2Price") Integer type2Price,
                           @Param("defaultType") String defaultType);
}
