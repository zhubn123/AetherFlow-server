package com.berlin.aetherflow.system.auth.config;

import cn.dev33.satoken.context.SaHolder;
import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import com.berlin.aetherflow.exception.ApiException;
import com.berlin.aetherflow.system.user.constant.PermissionConstants;
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
    private static final String PAGE_SUFFIX = "/page";

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

            SaRouter.match("/api/wms/**").check(r -> checkWmsPermission());
            SaRouter.match("/api/users/**").check(r -> checkUserPermission());
            SaRouter.match("/api/roles/**").check(r -> checkRolePermission());
            SaRouter.match("/api/permissions/**").check(r -> checkPermissionCatalogPermission());
        })).addPathPatterns("/**");
    }

    private void checkPermissionCatalogPermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }
        checkReadOnlyPermission(requestUri, method, PermissionConstants.SYSTEM_PERMISSION_VIEW);
    }

    private void checkRolePermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }
        checkResourcePermission(requestUri, method, PermissionConstants.SYSTEM_ROLE_VIEW,
                PermissionConstants.SYSTEM_ROLE_MANAGE);
    }

    private void checkUserPermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }

        if ("/api/users/profile".equals(requestUri) || "/api/users/password".equals(requestUri)) {
            return;
        }

        if ("/api/users/page".equals(requestUri)) {
            checkReadOnlyPermission(requestUri, method, PermissionConstants.SYSTEM_USER_VIEW);
            return;
        }

        if (requestUri.startsWith("/api/users/")) {
            checkResourcePermission(requestUri, method, PermissionConstants.SYSTEM_USER_VIEW,
                    PermissionConstants.SYSTEM_USER_MANAGE);
            return;
        }

        throw ApiException.forbidden("当前接口尚未配置权限规则，请联系管理员");
    }

    private void checkWmsPermission() {
        String requestUri = SaHolder.getRequest().getRequestPath();
        String method = SaHolder.getRequest().getMethod();
        if (requestUri == null) {
            return;
        }

        if (requestUri.startsWith("/api/wms/options")) {
            StpUtil.checkPermission(PermissionConstants.WMS_OPTION_VIEW);
            return;
        }

        if (requestUri.startsWith("/api/wms/warehouses")) {
            checkResourcePermission(requestUri, method, PermissionConstants.WMS_WAREHOUSE_VIEW,
                    PermissionConstants.WMS_WAREHOUSE_MANAGE);
            return;
        }

        if (requestUri.startsWith("/api/wms/areas")) {
            checkResourcePermission(requestUri, method, PermissionConstants.WMS_AREA_VIEW,
                    PermissionConstants.WMS_AREA_MANAGE);
            return;
        }

        if (requestUri.startsWith("/api/wms/locations")) {
            checkResourcePermission(requestUri, method, PermissionConstants.WMS_LOCATION_VIEW,
                    PermissionConstants.WMS_LOCATION_MANAGE);
            return;
        }

        if (requestUri.startsWith("/api/wms/materials")) {
            checkResourcePermission(requestUri, method, PermissionConstants.WMS_MATERIAL_VIEW,
                    PermissionConstants.WMS_MATERIAL_MANAGE);
            return;
        }

        if (requestUri.startsWith("/api/wms/inbound-orders")) {
            checkResourcePermission(requestUri, method, PermissionConstants.WMS_INBOUND_ORDER_VIEW,
                    PermissionConstants.WMS_INBOUND_ORDER_MANAGE);
            return;
        }

        if (requestUri.startsWith("/api/wms/outbound-orders")) {
            checkResourcePermission(requestUri, method, PermissionConstants.WMS_OUTBOUND_ORDER_VIEW,
                    PermissionConstants.WMS_OUTBOUND_ORDER_MANAGE);
            return;
        }

        if (requestUri.startsWith("/api/wms/inventory-adjustments")) {
            checkResourcePermission(requestUri, method, PermissionConstants.WMS_INVENTORY_ADJUSTMENT_VIEW,
                    PermissionConstants.WMS_INVENTORY_ADJUSTMENT_MANAGE);
            return;
        }

        if (requestUri.startsWith("/api/wms/stocks")) {
            checkReadOnlyPermission(requestUri, method, PermissionConstants.WMS_INVENTORY_VIEW);
            return;
        }

        if (requestUri.startsWith("/api/wms/stock-transactions")) {
            checkReadOnlyPermission(requestUri, method, PermissionConstants.WMS_STOCK_TRANSACTION_VIEW);
            return;
        }

        throw ApiException.forbidden("当前接口尚未配置权限规则，请联系管理员");
    }

    private void checkResourcePermission(String requestUri, String method, String viewPermission, String managePermission) {
        if (isLogicalReadRequest(requestUri, method)) {
            StpUtil.checkPermission(viewPermission);
            return;
        }
        StpUtil.checkPermission(managePermission);
    }

    private void checkReadOnlyPermission(String requestUri, String method, String viewPermission) {
        if (!isLogicalReadRequest(requestUri, method)) {
            throw ApiException.forbidden("当前资源仅允许读取访问");
        }
        StpUtil.checkPermission(viewPermission);
    }

    private boolean isLogicalReadRequest(String requestUri, String method) {
        if (method == null) {
            return false;
        }
        if (READ_METHODS.contains(method.toUpperCase(Locale.ROOT))) {
            return true;
        }
        return "POST".equalsIgnoreCase(method) && requestUri != null && requestUri.endsWith(PAGE_SUFFIX);
    }
}
