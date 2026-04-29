package com.berlin.aetherflow.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPI 文档配置。
 */
@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI aetherFlowOpenApi() {
        return new OpenAPI()
                // 绝大多数业务接口都要求登录，这里直接挂全局 Bearer 认证。
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components().addSecuritySchemes(
                        SECURITY_SCHEME_NAME,
                        new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                // Sa-Token 当前走的是 Bearer + token 值，不要求 JWT 结构。
                                .description("粘贴登录接口返回的 token 值即可，Swagger UI 会自动加上 Bearer 前缀。")
                ));
    }
}
