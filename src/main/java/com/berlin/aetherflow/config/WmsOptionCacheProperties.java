package com.berlin.aetherflow.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * WMS 主数据选项缓存配置。
 */
@Data
@Component
@ConfigurationProperties(prefix = "aether-flow.wms.option-cache")
public class WmsOptionCacheProperties {

    /**
     * Redis 缓存 TTL。
     */
    private Duration redisTtl = Duration.ofMinutes(10);

    /**
     * Redis 空结果 TTL。
     */
    private Duration redisEmptyTtl = Duration.ofSeconds(30);

    /**
     * Redis 失败后的重试退避时间。
     */
    private Duration redisFailureBackoff = Duration.ofSeconds(30);
}
