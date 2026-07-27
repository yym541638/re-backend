package com.compliancemind.soc.config;

import com.compliancemind.soc.common.constants.SocConstants;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Jackson 时间格式：兼容前端 {@code yyyy-MM-dd HH:mm:ss} 与 ISO-8601。
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter PRIMARY =
        DateTimeFormatter.ofPattern(SocConstants.Format.DATETIME_SECONDS);

    private static final List<DateTimeFormatter> PARSE_FORMATTERS = List.of(
        PRIMARY,
        DateTimeFormatter.ISO_LOCAL_DATE_TIME,
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm")
    );

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer localDateTimeCustomizer() {
        return builder -> {
            builder.serializers(new LocalDateTimeSerializer(PRIMARY));
            builder.deserializers(new FlexibleLocalDateTimeDeserializer());
        };
    }

    /** 优先按 {@code yyyy-MM-dd HH:mm:ss} 解析，失败再尝试 ISO 等常见格式。 */
    static final class FlexibleLocalDateTimeDeserializer extends StdDeserializer<LocalDateTime> {

        FlexibleLocalDateTimeDeserializer() {
            super(LocalDateTime.class);
        }

        @Override
        public LocalDateTime deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            String text = parser.getText();
            if (text == null || text.isBlank()) {
                return null;
            }
            String value = text.trim();
            if (value.length() == 10) {
                try {
                    return LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay();
                } catch (DateTimeParseException ignored) {
                    // fall through
                }
            }
            for (DateTimeFormatter formatter : PARSE_FORMATTERS) {
                try {
                    return LocalDateTime.parse(value, formatter);
                } catch (DateTimeParseException ignored) {
                    // try next
                }
            }
            throw context.weirdStringException(value, LocalDateTime.class,
                "支持格式: yyyy-MM-dd HH:mm:ss 或 ISO-8601，例如 2026-07-01 00:00:00");
        }
    }
}
