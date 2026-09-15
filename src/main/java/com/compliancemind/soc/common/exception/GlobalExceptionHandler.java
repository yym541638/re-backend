package com.compliancemind.soc.common.exception;

import com.compliancemind.soc.common.api.ApiResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常到 {@link com.compliancemind.soc.common.api.ApiResponse} 的映射（业务异常、参数校验等）。
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final MessageSource messageSource;

    public GlobalExceptionHandler(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler(BizException.class)
    public ApiResponse<Void> handleBizException(BizException exception) {
        String msg = messageSource.getMessage(
            exception.getMessageKey(),
            exception.getArgs(),
            exception.getMessageKey(),
            LocaleContextHolder.getLocale());
        return ApiResponse.fail(exception.getCode(), msg);
    }

    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        BindException.class,
        ConstraintViolationException.class,
        HttpMessageNotReadableException.class,
        MissingServletRequestParameterException.class,
        MethodArgumentTypeMismatchException.class
    })
    public ApiResponse<Void> handleBadRequest(Exception exception) {
        log.warn("Bad request: {}", exception.getMessage());
        String detail = resolveValidationMessage(exception);
        if (detail != null && !detail.isBlank()) {
            return ApiResponse.fail(BizErrorCode.COMMON_BAD_REQUEST.getCode(), detail);
        }
        String msg = messageSource.getMessage(
            BizErrorCode.COMMON_BAD_REQUEST.getMessageKey(),
            null,
            BizErrorCode.COMMON_BAD_REQUEST.getMessageKey(),
            LocaleContextHolder.getLocale());
        return ApiResponse.fail(BizErrorCode.COMMON_BAD_REQUEST.getCode(), msg);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ApiResponse<Void> handleDataIntegrity(DataIntegrityViolationException exception) {
        log.warn("Data integrity violation: {}", exception.getMostSpecificCause().getMessage());
        String raw = String.valueOf(exception.getMostSpecificCause().getMessage());
        if (raw.contains("uk_user_email") || raw.toLowerCase().contains("email")) {
            String msg = messageSource.getMessage(
                BizErrorCode.AUTH_EMAIL_REGISTERED.getMessageKey(),
                null,
                BizErrorCode.AUTH_EMAIL_REGISTERED.getMessageKey(),
                LocaleContextHolder.getLocale());
            return ApiResponse.fail(BizErrorCode.AUTH_EMAIL_REGISTERED.getCode(), msg);
        }
        if (raw.contains("uk_user_phone") || raw.toLowerCase().contains("phone")) {
            String msg = messageSource.getMessage(
                BizErrorCode.AUTH_PHONE_REGISTERED.getMessageKey(),
                null,
                BizErrorCode.AUTH_PHONE_REGISTERED.getMessageKey(),
                LocaleContextHolder.getLocale());
            return ApiResponse.fail(BizErrorCode.AUTH_PHONE_REGISTERED.getCode(), msg);
        }
        String msg = messageSource.getMessage(
            BizErrorCode.COMMON_BAD_REQUEST.getMessageKey(),
            null,
            "数据冲突，请检查是否与已有记录重复",
            LocaleContextHolder.getLocale());
        return ApiResponse.fail(BizErrorCode.COMMON_BAD_REQUEST.getCode(), msg);
    }

    private String resolveValidationMessage(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException manv) {
            if (manv.getBindingResult().getFieldError() != null) {
                return manv.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        if (exception instanceof BindException bindException) {
            if (bindException.getBindingResult().getFieldError() != null) {
                return bindException.getBindingResult().getFieldError().getDefaultMessage();
            }
        }
        if (exception instanceof ConstraintViolationException cve) {
            return cve.getConstraintViolations().stream()
                .findFirst()
                .map(violation -> violation.getMessage())
                .orElse(null);
        }
        if (exception instanceof MissingServletRequestParameterException missing) {
            return "缺少必填参数: " + missing.getParameterName();
        }
        if (exception instanceof MethodArgumentTypeMismatchException mismatch) {
            return "参数类型错误: " + mismatch.getName();
        }
        return null;
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleException(Exception exception) {
        log.error("Unhandled exception", exception);
        String msg = messageSource.getMessage(
            BizErrorCode.COMMON_INTERNAL_ERROR.getMessageKey(),
            null,
            BizErrorCode.COMMON_INTERNAL_ERROR.getMessageKey(),
            LocaleContextHolder.getLocale());
        return ApiResponse.fail(BizErrorCode.COMMON_INTERNAL_ERROR.getCode(), msg);
    }
}
