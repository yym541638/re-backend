package com.compliancemind.soc.common.exception;

import lombok.Getter;

/**
 * 可预期的业务异常，携带 HTTP 语义 {@code code} 与 Spring {@link org.springframework.context.MessageSource MessageSource} 文案键。
 */
@Getter
public class BizException extends RuntimeException {

    private final int code;
    private final String messageKey;
    private final Object[] args;

    public BizException(BizErrorCode errorCode, Object... args) {
        super(errorCode.name());
        this.code = errorCode.getCode();
        this.messageKey = errorCode.getMessageKey();
        this.args = args == null ? new Object[0] : args.clone();
    }
}
