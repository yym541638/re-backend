package com.compliancemind.soc.common.api;

import com.compliancemind.soc.common.constants.SocConstants;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 成功响应时若 {@code message} 为空则填充国际化成功文案。
 */
@RestControllerAdvice
public class ApiSuccessMessageAdvice implements ResponseBodyAdvice<ApiResponse<?>> {

    private final MessageSource messageSource;

    public ApiSuccessMessageAdvice(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }

    @Override
    public ApiResponse<?> beforeBodyWrite(ApiResponse<?> body,
                                          MethodParameter returnType,
                                          MediaType selectedContentType,
                                          Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                          ServerHttpRequest request,
                                          ServerHttpResponse response) {
        if (body != null && body.getCode() == SocConstants.Api.SUCCESS_CODE && (body.getMessage() == null || body.getMessage().isEmpty())) {
            body.setMessage(messageSource.getMessage("api.success", null, LocaleContextHolder.getLocale()));
        }
        return body;
    }
}
