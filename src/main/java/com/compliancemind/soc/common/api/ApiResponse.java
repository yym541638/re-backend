package com.compliancemind.soc.common.api;

import com.compliancemind.soc.common.constants.SocConstants;
import lombok.Data;

/**
 * 统一 API 响应体：{@code code}、{@code message}、{@code data}。
 */
@Data
public class ApiResponse<T> {

    private int code;
    private String message;
    private T data;
    /** 分页接口可选：与 {@code data} 内总条数一致，便于前端从根节点读取。 */
    private Long total;

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(SocConstants.Api.SUCCESS_CODE);
        response.setMessage(null);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<PageResponse<T>> page(PageResponse<T> pageData) {
        ApiResponse<PageResponse<T>> response = success(pageData);
        if (pageData != null) {
            response.setTotal(pageData.getTotalCount());
        }
        return response;
    }

    public static ApiResponse<Void> success() {
        return success(null);
    }

    public static <T> ApiResponse<T> fail(int code, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(code);
        response.setMessage(message);
        response.setData(null);
        return response;
    }
}

