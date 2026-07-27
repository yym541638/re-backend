package com.compliancemind.soc.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SpringDoc OpenAPI / Swagger UI：扫描全部 Controller，默认 Bearer JWT。
 *
 * <p>访问：{@code /api/swagger-ui.html}；先调 {@code POST /auth/login} 取 token，
 * 再点 Authorize 填入 {@code Bearer <token>} 或仅填 token。</p>
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("SOC Compliance API")
                .description("""
                    SOC 合规自动化后端接口。

                    ## 鉴权说明
                    1. 调用 `POST /auth/login` 获取 `data.token`
                    2. 点击右上角 **Authorize**
                    3. 在 Value 中填入 JWT（可只填 token，或填 `Bearer <token>`）
                    4. 再调试其余需登录接口

                    统一响应：`{ code, message, data, total? }`，`code=0` 表示成功。
                    """)
                .version("0.1.0"))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")
                    .description("登录接口返回的 JWT token")));
    }
}
