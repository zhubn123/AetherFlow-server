package com.berlin.aetherflow.system.monitor.startup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.*;
import java.util.regex.Pattern;

/**
 * 在应用启动完成后扫描所有接口映射，并对不符合 RESTful 风格的路径输出告警。
 */
@Slf4j
@Component
public class RestfulApiNamingInspector {

    private static final Pattern UPPERCASE_PATTERN = Pattern.compile(".*[A-Z].*");
    private static final Pattern UNDERSCORE_PATTERN = Pattern.compile(".*_.*");
    private static final Pattern VERB_PREFIX_PATTERN = Pattern.compile(".*/(get|add|create|update|delete|remove|list)[A-Z]?.*");
    private static final Set<String> ALLOWED_NON_RESOURCE_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/logout",
            "/api/health"
    );

    private final RequestMappingHandlerMapping handlerMapping;

    public RestfulApiNamingInspector(RequestMappingHandlerMapping handlerMapping) {
        this.handlerMapping = handlerMapping;
    }

    /**
     * 扫描所有 RequestMapping，并输出接口命名规范检查结果。
     */
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @EventListener(ApplicationReadyEvent.class)
    public void inspect() {
        List<String> invalidPaths = new ArrayList<>();

        for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMapping.getHandlerMethods().entrySet()) {
            RequestMappingInfo mappingInfo = entry.getKey();
            HandlerMethod handlerMethod = entry.getValue();

            if (!isProjectController(handlerMethod)) {
                continue;
            }

            for (String path : mappingInfo.getPatternValues()) {
                validatePath(path, mappingInfo, invalidPaths);
            }
        }

        if (invalidPaths.isEmpty()) {
            log.info("RESTful API naming inspection passed with no warnings.");
            return;
        }

        log.warn("""
                以下路径不符合 RESTful 风格，应该使用统一的小写、资源导向 URL：
                {}
                """, String.join(System.lineSeparator(), invalidPaths));
    }

    /**
     * 判断当前映射是否属于本项目的业务 Controller。
     *
     * @param handlerMethod 当前处理器方法
     * @return 是否为项目内接口
     */
    private boolean isProjectController(HandlerMethod handlerMethod) {
        String packageName = handlerMethod.getBeanType().getPackageName();
        return packageName.startsWith("com.berlin.aetherflow.modules");
    }

    /**
     * 校验单个接口路径是否符合当前 RESTful 命名约定。
     *
     * @param path        接口路径
     * @param mappingInfo 当前映射信息
     * @param invalidPaths 不符合规范的路径收集器
     */
    private void validatePath(String path, RequestMappingInfo mappingInfo, List<String> invalidPaths) {
        if (path == null || path.isBlank()) {
            return;
        }

        String normalized = path.toLowerCase(Locale.ROOT);
        boolean invalid = !path.startsWith("/api")
                || UPPERCASE_PATTERN.matcher(path).matches()
                || UNDERSCORE_PATTERN.matcher(path).matches()
                || (!ALLOWED_NON_RESOURCE_PATHS.contains(normalized) && VERB_PREFIX_PATTERN.matcher(path).matches());

        if (invalid) {
            invalidPaths.add("- " + describe(path, mappingInfo));
        }
    }

    /**
     * 将请求方法和路径拼装成便于日志输出的描述文本。
     *
     * @param path        接口路径
     * @param mappingInfo 当前映射信息
     * @return 路径描述
     */
    private String describe(String path, RequestMappingInfo mappingInfo) {
        Set<RequestMethod> methods = mappingInfo.getMethodsCondition().getMethods();
        String methodText = methods.isEmpty() ? "ALL" : methods.toString();
        return methodText + " " + path;
    }
}
