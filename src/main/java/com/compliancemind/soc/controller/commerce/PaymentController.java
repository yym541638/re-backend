package com.compliancemind.soc.controller.commerce;



import com.compliancemind.soc.common.api.ApiResponse;

import com.compliancemind.soc.dto.commerce.PaymentStatusResponse;

import com.compliancemind.soc.dto.commerce.PaymentSubmitRequest;

import com.compliancemind.soc.dto.commerce.PaymentSubmitResponse;

import com.compliancemind.soc.service.commerce.PaymentService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;



import java.util.Map;



/**

 * 支付：下单、查询、异步通知（多字段名兼容）、模拟支付成功跳转。

 * <p>对应 PRD 2.3 Checkout 支付页。</p>

 */

@RestController

@RequestMapping("/payment")

public class PaymentController {



    private final PaymentService paymentService;



    public PaymentController(PaymentService paymentService) {

        this.paymentService = paymentService;

    }



    /**

     * 提交支付（含创建订单）（PRD 2.3）。

     * <p>POST /payment/submit，需 JWT；不传 orderNo 时后端自动生成。</p>

     * @param request 产品、套餐、审计类型、支付方式及回调 URL

     * @return 订单号及支付跳转信息

     */

    @PostMapping("/submit")

    public ApiResponse<PaymentSubmitResponse> submit(@Valid @RequestBody PaymentSubmitRequest request) {

        return ApiResponse.success(paymentService.submit(request));

    }



    /**

     * 查询支付状态（PRD 2.3）。

     * <p>GET /payment/query/{orderNo}，匿名访问；轮询订单支付结果。</p>

     *

     * @param orderNo 订单号

     * @return 支付状态及交易信息

     */

    @GetMapping("/query/{orderNo}")

    public ApiResponse<PaymentStatusResponse> query(@PathVariable("orderNo") String orderNo) {

        return ApiResponse.success(paymentService.query(orderNo));

    }



    /**

     * 支付异步通知（PRD 2.3）。

     * <p>POST /payment/notify，匿名访问；兼容多种支付网关字段名。</p>

     *

     * @param payload 支付网关回调体（orderNo/status/transactionId 等）

     * @return 处理成功空响应

     */

    @PostMapping("/notify")

    public ApiResponse<Void> notify(@RequestBody Map<String, Object> payload) {

        paymentService.handleNotify(

            firstNonBlank(payload, "orderNo", "order_no", "outTradeNo", "out_trade_no"),

            firstNonBlank(payload, "status", "tradeStatus", "trade_status", "payStatus", "pay_status"),

            firstNonBlank(payload, "transactionId", "transaction_id", "tradeNo", "trade_no")

        );

        return ApiResponse.success();

    }



    /**

     * 模拟支付成功跳转（PRD 2.3，开发/测试用）。

     * <p>GET /payment/mock/success，匿名访问；模拟支付完成并返回跳转 URL。</p>

     *

     * @param orderNo         订单号（camelCase）

     * @param orderNoLegacy   订单号（snake_case 别名）

     * @param returnUrl       支付完成跳转 URL

     * @param returnUrlLegacy 跳转 URL（snake_case 别名）

     * @return 跳转地址

     */

    @GetMapping("/mock/success")

    public ApiResponse<String> mockSuccess(@RequestParam(value = "orderNo", required = false) String orderNo,

                                           @RequestParam(value = "order_no", required = false) String orderNoLegacy,

                                           @RequestParam(value = "returnUrl", required = false) String returnUrl,

                                           @RequestParam(value = "return_url", required = false) String returnUrlLegacy) {

        return ApiResponse.success(paymentService.mockSuccess(orderNo != null ? orderNo : orderNoLegacy, returnUrl != null ? returnUrl : returnUrlLegacy));

    }



    /** 支付网关回调体字段名各异，按候选键依次取首个非空值。 */

    private String firstNonBlank(Map<String, Object> payload, String... keys) {

        for (String key : keys) {

            Object value = payload.get(key);

            if (value != null && !String.valueOf(value).isBlank()) {

                return String.valueOf(value);

            }

        }

        return null;

    }

}


