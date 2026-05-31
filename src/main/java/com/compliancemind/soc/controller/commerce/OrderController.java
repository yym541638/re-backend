package com.compliancemind.soc.controller.commerce;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.entity.commerce.OrderRecord;

import com.compliancemind.soc.service.commerce.OrderService;

import org.springframework.web.bind.annotation.DeleteMapping;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PutMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;



import java.util.List;



/**

 * 订单：我的订单列表、详情与取消（兼容 PUT/DELETE 取消）。

 * <p>对应 PRD 2.3 Checkout 支付页（订单详情）。</p>

 */

@RestController

@RequestMapping("/order")

public class OrderController {



    private final OrderService orderService;



    public OrderController(OrderService orderService) {

        this.orderService = orderService;

    }



    /**

     * 我的订单列表（扩展）。

     * <p>GET /order/my，需 JWT；返回当前用户全部订单。</p>

     *

     * @return 订单列表

     */

    @GetMapping("/my")

    public ApiResponse<List<OrderRecord>> myOrders() {

        return ApiResponse.success(orderService.myOrders());

    }



    /**

     * 订单详情（PRD 2.3）。

     * <p>GET /order/detail/{orderNo}，需 JWT；支付完成后查看订单信息。</p>

     *

     * @param orderNo 订单号

     * @return 订单详情

     */

    @GetMapping("/detail/{orderNo}")

    public ApiResponse<OrderRecord> detail(@PathVariable("orderNo") String orderNo) {

        return ApiResponse.success(orderService.detail(orderNo));

    }



//    /**
//
//     * 取消订单（扩展）。
//
//     * <p>DELETE /order/cancel/{orderNo}，需 JWT；取消未支付订单。</p>
//
//     *
//
//     * @param orderNo 订单号
//
//     * @return 操作成功空响应
//
//     */
//
//    @DeleteMapping("/cancel/{orderNo}")
//
//    public ApiResponse<Void> cancel(@PathVariable("orderNo") String orderNo) {
//
//        orderService.cancel(orderNo);
//
//        return ApiResponse.success();
//
//    }
//
//
//
//    /**
//
//     * 取消订单（旧版兼容路径）。
//
//     * <p>PUT /order/cancel/{orderNo}，需 JWT；与 DELETE 行为一致。</p>
//
//     *
//
//     * @param orderNo 订单号
//
//     * @return 操作成功空响应
//
//     */
//
//    @PutMapping("/cancel/{orderNo}")
//
//    public ApiResponse<Void> legacyCancel(@PathVariable("orderNo") String orderNo) {
//
//        orderService.cancel(orderNo);
//
//        return ApiResponse.success();
//
//    }

}


