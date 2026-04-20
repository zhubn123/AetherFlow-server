package com.berlin.aetherflow.system.auth.config;

import org.springframework.context.annotation.Configuration;

/**
 * 当前阶段先保持 Sa-Token 使用默认自动配置。
 * 后续接入登录态、权限码、Redis 持久化时，再补自定义逻辑实现。
 */
@Configuration
public class SaTokenConfig {
}
