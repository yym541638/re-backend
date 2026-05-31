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

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setCode(SocConstants.Api.SUCCESS_CODE);
        response.setMessage(null);
        response.setData(data);
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

