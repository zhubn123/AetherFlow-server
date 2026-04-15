package com.berlin.aetherflow.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * LoggingInterceptor
 *
 * @author zhubn
 * @date 2026/4/15
 */
@Slf4j
@Component
public class LoggingInterceptor implements HandlerInterceptor {
    private static final String START_TIME_ATTR = "request.startTime";
    private static final String MASKED_VALUE = "***";
    private static final Set<String> SENSITIVE_PARAMETER_KEYWORDS = Set.of(
            "password",
            "pwd",
            "token",
            "secret",
            "authorization",
            "credential"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_TIME_ATTR, System.currentTimeMillis());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        long startedAt = request.getAttribute(START_TIME_ATTR) instanceof Long
                ? (Long) request.getAttribute(START_TIME_ATTR)
                : System.currentTimeMillis();
        long durationMs = System.currentTimeMillis() - startedAt;

        log.info(
                "sample request method={} uri={} status={} durationMs={} query={} params={}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                durationMs,
                formatQueryString(request.getQueryString()),
                formatParameters(request.getParameterMap())
        );

        if (ex != null) {
            log.error("sample request failed uri={}", request.getRequestURI(), ex);
        }
    }

    private String formatParameters(Map<String, String[]> parameterMap) {
        if (parameterMap == null || parameterMap.isEmpty()) {
            return "-";
        }
        return parameterMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + formatParameterValues(entry.getKey(), entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    private String formatParameterValues(String parameterName, String[] values) {
        if (values == null || values.length == 0) {
            return "[]";
        }
        if (isSensitiveParameter(parameterName)) {
            String[] maskedValues = new String[values.length];
            Arrays.fill(maskedValues, MASKED_VALUE);
            return Arrays.toString(maskedValues);
        }
        return Arrays.toString(values);
    }

    private String formatQueryString(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return "-";
        }
        return Arrays.stream(queryString.split("&"))
                .map(this::maskQueryPart)
                .collect(Collectors.joining("&"));
    }

    private String maskQueryPart(String queryPart) {
        int separatorIndex = queryPart.indexOf('=');
        if (separatorIndex < 0) {
            return queryPart;
        }

        String parameterName = queryPart.substring(0, separatorIndex);
        if (isSensitiveParameter(parameterName)) {
            return parameterName + "=" + MASKED_VALUE;
        }

        return queryPart;
    }

    private boolean isSensitiveParameter(String parameterName) {
        if (parameterName == null || parameterName.isBlank()) {
            return false;
        }

        String normalizedName = parameterName.toLowerCase(Locale.ROOT);
        return SENSITIVE_PARAMETER_KEYWORDS.stream().anyMatch(normalizedName::contains);
    }
}
