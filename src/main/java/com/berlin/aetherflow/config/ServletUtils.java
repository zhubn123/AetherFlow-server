package com.berlin.aetherflow.config;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ServletUtils
 *
 * @author zhubn
 * @date 2026/4/18
 */
public class ServletUtils {

    /**
     * 获取客户端真实IP地址
     *
     * @param request HttpServletRequest对象
     * @return 真实IP地址
     */
    public static String getClientIpAddress(HttpServletRequest request) {
        String ip = null;

        // 按优先级依次检查可能包含真实IP的请求头
        String[] headers = {
                "X-Forwarded-For",      // 标准的代理服务器头
                "X-Real-IP",            // Nginx常用的真实IP头
                "Proxy-Client-IP",      // Apache代理头
                "WL-Proxy-Client-IP",   // WebLogic代理头
                "HTTP_CLIENT_IP",       // 一些代理服务器使用的头
                "HTTP_X_FORWARDED_FOR"  // 兼容某些代理服务器
        };

        for (String header : headers) {
            ip = request.getHeader(header);
            if (ip != null && !ip.trim().isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                break;
            }
        }

        // 如果从请求头中获取不到IP，则使用getRemoteAddr()
        if (ip == null || ip.trim().isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // X-Forwarded-For可能包含多个IP地址，格式为 "client, proxy1, proxy2"
        // 第一个IP地址是真实的客户端IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }
}