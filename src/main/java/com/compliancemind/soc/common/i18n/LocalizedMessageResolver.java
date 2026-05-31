package com.compliancemind.soc.common.i18n;

import com.compliancemind.soc.common.exception.BizErrorCode;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

/**
 * 基于当前语言环境解析 {@link MessageSource} 文案（供非 Web 层或工具使用）。
 */
@Component
public class LocalizedMessageResolver {

    private final MessageSource messageSource;

    public LocalizedMessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String message(BizErrorCode code, Object... args) {
        return message(code.getMessageKey(), args);
    }

    public String message(String messageKey, Object... args) {
        return messageSource.getMessage(messageKey, args, LocaleContextHolder.getLocale());
    }
}
