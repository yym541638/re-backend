package com.compliancemind.soc.controller.commerce;


import com.compliancemind.soc.dto.commerce.*;

import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.service.commerce.ProductService;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;


import java.util.List;


/**
 * 商品：列表、详情（审计类型）、套餐调价（公司管理员）、我的订阅。
 *
 * <p>对应 PRD 2.2.1 选择产品、2.2.2 购买产品（定价页）。</p>
 */

@RestController

@RequestMapping("/product")

public class ProductController {


    private final ProductService productService;


    public ProductController(ProductService productService) {

        this.productService = productService;

    }


//    /**
//     * 在售产品列表（PRD 2.2.2）。
//     * <p>GET /product/list，匿名访问；用于定价页产品下拉/卡片展示。</p>
//     * @return 在售产品列表   这个接口单纯展示产品信息和能力,不需要
//     */
//    @GetMapping("/list")
//
//    public ApiResponse<List<ProductListItem>> list() {
//
//        return ApiResponse.success(productService.list());
//
//    }

    /**
     * SOC 2 套餐列表与动态计价（PRD 2.2.2 购买页）。
     *
     * <p>GET /product/packages，匿名访问；新用户进入购买页时展示全部 SOC 2 套餐，无需 productId。</p>
     *
     * @param auditType       审计类型（camelCase，如 Type1 / Type2）
     * @param auditTypeLegacy 审计类型（snake_case 别名 audit_type）
     * @return SOC 2 套餐列表及对应价格
     */

    @GetMapping("/packages")

    public ApiResponse<List<ProductDetail2Response>> packages(@RequestParam(value = "auditType", required = false) String auditType,

                                                              @RequestParam(value = "audit_type", required = false) String auditTypeLegacy) {

        return ApiResponse.success(productService.listSoc2Packages(auditType != null ? auditType : auditTypeLegacy));

    }


    /**
     * 用户已购产品详情展示。
     *
     * <p>GET /product/detail?productId=xxx，需 JWT；基于当前用户已购记录返回展示信息。</p>
     *
     * @param productId 产品 ID（Query 参数）
     * @return 当前用户已购产品详情
     */
    @GetMapping("/detail")
    public ApiResponse<List<ProductDetail2Response>> detail(@RequestParam("productId") Integer productId) {
        return ApiResponse.success(productService.purchasedDetail(productId));
    }


    /**
     * 已购产品/模块列表（PRD 2.2.1）。
     * <p>GET /product/my，需 JWT；用于 SOC2 选择产品页展示用户已订阅模块。</p>
     *
     * @return 当前用户已购产品及套餐信息
     */

    @GetMapping("/my")

    public ApiResponse<List<UserProductItem>> myProducts() {
        return ApiResponse.success(productService.myProducts());
    }


}


