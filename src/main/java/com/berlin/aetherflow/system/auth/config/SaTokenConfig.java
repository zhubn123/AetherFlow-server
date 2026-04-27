package com.berlin.aetherflow.system.auth.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Locale;
import java.util.Set;

/**
 * Sa-Token 鉴权配置。
 */
@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    private static final Set<String> READ_METHODS = Set.of("GET", "HEAD", "OPTIONS");

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handler -> {
            SaRouter.match("/api/**")
                    .notMatch(
                            "/api/auth/login",
                            "/api/auth/register",
                            "/api/auth/refresh",
                            "/api/health",
                            "/v3/api-docs",
                            "/v3/api-docs/**",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/error"
                    )
                    .check(r -> StpUtil.checkLogin());

            SaRouter.match("/api/wms/**").check(r -> checkWmsRoleByMethod());
        })).addPathPatterns("/**");
    }

    private void checkWmsRoleByMethod() {
        boolean hasReadRole = StpUtil.hasRole("admin")
                || StpUtil.hasRole("operator")
                || StpUtil.hasRole("viewer");
        if (!hasReadRole) {
            // 复用 Sa-Token 的角色异常返回口径。
            StpUtil.checkRole("admin");
            return;
        }

        String method = SaHolder.getRequest().getMethod();
        if (method == null) {
            return;
        }

        if (READ_METHODS.contains(method.toUpperCase(Locale.ROOT))) {
            return;
        }

        boolean hasWriteRole = StpUtil.hasRole("admin") || StpUtil.hasRole("operator");
        if (!hasWriteRole) {
            StpUtil.checkRole("admin");
        }
    }
}
