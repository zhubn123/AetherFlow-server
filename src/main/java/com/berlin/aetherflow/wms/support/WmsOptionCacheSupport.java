package com.berlin.aetherflow.wms.support;

import com.berlin.aetherflow.config.WmsOptionCacheProperties;
import com.berlin.aetherflow.wms.domain.vo.WmsOptionVo;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

/**
 * WMS 主数据选项 Redis 缓存支持。
 */
@Slf4j
@Component
public class WmsOptionCacheSupport {

    public static final String NAMESPACE_WAREHOUSE = "warehouse";
    public static final String NAMESPACE_AREA = "area";
    public static final String NAMESPACE_LOCATION = "location";
    public static final String NAMESPACE_MATERIAL = "material";

    private static final String REDIS_KEY_PREFIX = "wms:options:";
    private static final TypeReference<List<WmsOptionVo>> OPTION_LIST_TYPE = new TypeReference<>() {
    };

    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;
    private final ObjectMapper objectMapper;
    private final WmsOptionCacheProperties properties;
    private final AtomicReference<Instant> redisRetryAfter = new AtomicReference<>(Instant.EPOCH);

    public WmsOptionCacheSupport(ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider,
                                 ObjectMapper objectMapper,
                                 WmsOptionCacheProperties properties) {
        this.stringRedisTemplateProvider = stringRedisTemplateProvider;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    // TODO 防缓存击穿：后续如热点 options key 并发过高，
    // 可在 Redis miss 后增加短时分布式锁，保证同一 key 只有一个请求回源 DB。
    // 当前 WMS 主数据 options 并发较低，暂不引入锁复杂度。
    // ===================== 防缓存击穿（分布式锁方案） =====================
    //
    // TODO 启用条件：热点 key 并发较高时开启
    //
    // 使用方式：在 Redis miss 后调用该逻辑，保证同一 key 只有一个线程回源 DB
    //
    // 注意：
    // 1. 一定要双重检查 Redis（Double Check）
    // 2. 锁必须设置过期时间（防死锁）
    // 3. 解锁必须校验 value（防误删）
    /*
public List<WmsOptionVo> getOrLoadWithLock(
        String namespace,
        String cacheKey,
        Supplier<List<WmsOptionVo>> loader) {

    String redisKey = buildRedisKey(namespace, cacheKey);

    // 1. 先查 Redis
    List<WmsOptionVo> redisValue = readRedis(redisKey);
    if (redisValue != null) {
        return redisValue;
    }

    String lockKey = redisKey + ":lock";
    String lockValue = UUID.randomUUID().toString();

    // 2. 尝试加锁
    if (tryLock(lockKey, lockValue)) {
        try {
            // 3. 双重检查
            redisValue = readRedis(redisKey);
            if (redisValue != null) {
                return redisValue;
            }

            // 4. 查 DB
            List<WmsOptionVo> loaded = sanitize(loader.get());

            // 5. 写缓存
            writeRedis(redisKey, loaded);

            return loaded;

        } finally {
            unlock(lockKey, lockValue);
        }
    }

    // 6. 没抢到锁 → 短暂等待
    sleepQuietly(50);

    redisValue = readRedis(redisKey);
    if (redisValue != null) {
        return redisValue;
    }

    // 7. 兜底（避免一直等）
    List<WmsOptionVo> loaded = sanitize(loader.get());
    writeRedis(redisKey, loaded);
    return loaded;
}

private boolean tryLock(String lockKey, String lockValue) {
    StringRedisTemplate redis = stringRedisTemplateProvider.getIfAvailable();
    if (redis == null || isRedisBackoffActive()) {
        return false;
    }

    Boolean success = redis.opsForValue()
            .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(3));

    return Boolean.TRUE.equals(success);
}

private void unlock(String lockKey, String lockValue) {
    StringRedisTemplate redis = stringRedisTemplateProvider.getIfAvailable();
    if (redis == null) {
        return;
    }

    try {
        String currentValue = redis.opsForValue().get(lockKey);
        if (lockValue.equals(currentValue)) {
            redis.delete(lockKey);
        }
    } catch (Exception ignore) {
    }
}

private void sleepQuietly(long millis) {
    try {
        Thread.sleep(millis);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
    }
}
*/

    // TODO 防缓存击穿进阶方案：热点 options key 可改为逻辑过期。
    // Redis value 中保存 data + expireTime，物理 TTL 设置更长。
    // 当逻辑过期时，请求先返回旧数据，只有抢到锁的线程异步重建缓存，
    // 其他线程不等待、不回源 DB，避免热点 key 击穿。
    // ===================== 防缓存击穿（逻辑过期方案） =====================
    //
    // TODO 启用条件：热点 key 且允许短时间旧数据
    //
    // 核心思想：
    // - Redis 不删除数据
    // - value 中存 expireTime
    // - 过期时返回旧数据 + 异步重建
    //
    // 注意：
    // 1. Redis TTL 要比逻辑 TTL 长
    // 2. 必须配合分布式锁
    // 3. 要有线程池（不要阻塞主线程）
    /*
    @Data
    public static class CacheEntry<T> {
        private T data;
        private Instant expireTime;
    }
    public List<WmsOptionVo> getOrLoadWithLogicalExpire(
            String namespace,
            String cacheKey,
            Supplier<List<WmsOptionVo>> loader) {

        String redisKey = buildRedisKey(namespace, cacheKey);

        CacheEntry<List<WmsOptionVo>> entry = readRedisEntry(redisKey);

        if (entry == null) {
            List<WmsOptionVo> loaded = sanitize(loader.get());
            writeRedisEntry(redisKey, loaded);
            return loaded;
        }

        // 1. 未过期 → 直接返回
        if (entry.getExpireTime().isAfter(Instant.now())) {
            return sanitize(entry.getData());
        }

        // 2. 已过期 → 尝试异步刷新
        String lockKey = redisKey + ":lock";
        String lockValue = UUID.randomUUID().toString();

        if (tryLock(lockKey, lockValue)) {
            CompletableFuture.runAsync(() -> {
                try {
                    List<WmsOptionVo> fresh = sanitize(loader.get());
                    writeRedisEntry(redisKey, fresh);
                } finally {
                    unlock(lockKey, lockValue);
                }
            });
        }

        // 3. 返回旧数据（关键）
        return sanitize(entry.getData());
    }

    private void writeRedisEntry(String redisKey, List<WmsOptionVo> data) {
        StringRedisTemplate redis = stringRedisTemplateProvider.getIfAvailable();
        if (redis == null) {
            return;
        }

        try {
            CacheEntry<List<WmsOptionVo>> entry = new CacheEntry<>();
            entry.setData(data);
            entry.setExpireTime(Instant.now().plus(properties.getRedisTtl()));

            String json = objectMapper.writeValueAsString(entry);

            // 注意：Redis TTL 要比逻辑 TTL 长
            redis.opsForValue().set(redisKey, json, properties.getRedisTtl().multipliedBy(3));

        } catch (Exception ignore) {
        }
    }

    private CacheEntry<List<WmsOptionVo>> readRedisEntry(String redisKey) {
        StringRedisTemplate redis = stringRedisTemplateProvider.getIfAvailable();
        if (redis == null) {
            return null;
        }

        try {
            String json = redis.opsForValue().get(redisKey);
            if (json == null) {
                return null;
            }

            return objectMapper.readValue(json,
                    new TypeReference<CacheEntry<List<WmsOptionVo>>>() {});
        } catch (Exception e) {
            return null;
        }
    }
    */

    public List<WmsOptionVo> getOrLoad(String namespace, String cacheKey, Supplier<List<WmsOptionVo>> loader) {
        String redisKey = buildRedisKey(namespace, cacheKey);

        // 现阶段采用最简单可靠的路径：
        // 先查 Redis，miss 后回源 DB，再把结果写回 Redis。
        List<WmsOptionVo> redisValue = readRedis(redisKey);
        if (redisValue != null) {
            return redisValue;
        }

        List<WmsOptionVo> loaded = sanitize(loader.get());
        writeRedis(redisKey, loaded);
        return loaded;
    }

    public void evictWarehouseRelatedOptions() {
        evictNamespaces(NAMESPACE_WAREHOUSE, NAMESPACE_AREA, NAMESPACE_LOCATION);
    }

    public void evictAreaRelatedOptions() {
        evictNamespaces(NAMESPACE_AREA, NAMESPACE_LOCATION);
    }

    public void evictLocationOptions() {
        evictNamespaces(NAMESPACE_LOCATION);
    }

    public void evictMaterialOptions() {
        evictNamespaces(NAMESPACE_MATERIAL);
    }

    private void evictNamespaces(String... namespaces) {
        if (namespaces == null || namespaces.length == 0) {
            return;
        }

        List<String> normalizedNamespaces = Arrays.stream(namespaces)
                .filter(namespace -> namespace != null && !namespace.isBlank())
                .distinct()
                .toList();
        if (normalizedNamespaces.isEmpty()) {
            return;
        }

        StringRedisTemplate stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (stringRedisTemplate == null || isRedisBackoffActive()) {
            return;
        }

        try {
            for (String namespace : normalizedNamespaces) {
                String pattern = buildRedisKey(namespace, "*");
                Set<String> keys = stringRedisTemplate.keys(pattern);
                if (keys == null || keys.isEmpty()) {
                    continue;
                }
                stringRedisTemplate.delete(keys);
            }
            redisRetryAfter.set(Instant.EPOCH);
        } catch (Exception ex) {
            markRedisUnavailable("缓存失效", "batch", ex);
        }
    }

    private List<WmsOptionVo> readRedis(String redisKey) {
        StringRedisTemplate stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (stringRedisTemplate == null || isRedisBackoffActive()) {
            return null;
        }

        try {
            String json = stringRedisTemplate.opsForValue().get(redisKey);
            if (json == null || json.isBlank()) {
                return null;
            }
            List<WmsOptionVo> parsed = objectMapper.readValue(json, OPTION_LIST_TYPE);
            redisRetryAfter.set(Instant.EPOCH);
            return sanitize(parsed);
        } catch (Exception ex) {
            markRedisUnavailable("缓存读取", redisKey, ex);
            return null;
        }
    }

    private void writeRedis(String redisKey, List<WmsOptionVo> value) {
        StringRedisTemplate stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (stringRedisTemplate == null || isRedisBackoffActive()) {
            return;
        }

        try {
            // 空结果也会写缓存，但 TTL 更短，用来挡住同一类不存在条件的反复回源。
            List<WmsOptionVo> safeValue = sanitize(value);
            String json = objectMapper.writeValueAsString(safeValue);
            Duration ttl = buildTtl(safeValue);
            stringRedisTemplate.opsForValue().set(redisKey, json, ttl);
            redisRetryAfter.set(Instant.EPOCH);
        } catch (Exception ex) {
            markRedisUnavailable("缓存写入", redisKey, ex);
        }
    }

    private boolean isRedisBackoffActive() {
        return redisRetryAfter.get().isAfter(Instant.now());
    }

    private void markRedisUnavailable(String action, String key, Exception ex) {
        redisRetryAfter.set(Instant.now().plus(properties.getRedisFailureBackoff()));
        log.warn("Redis 主数据选项{}失败，降级直查数据库，key={}, reason={}", action, key, ex.getMessage());
        log.warn("Redis 主数据选项缓存将在 {} 后重试连接", properties.getRedisFailureBackoff());
        log.debug("Redis 主数据选项{}异常明细，key={}", action, key, ex);
    }

    private Duration buildTtl(List<WmsOptionVo> value) {
        Duration baseTtl = value.isEmpty()
                ? properties.getRedisEmptyTtl()
                : properties.getRedisTtl();
        // 加一点随机抖动，避免同一批 key 同时过期造成缓存雪崩。
        long jitterSeconds = ThreadLocalRandom.current().nextLong(0, 60);
        return baseTtl.plusSeconds(jitterSeconds);
    }

    private String buildRedisKey(String namespace, String cacheKey) {
        return REDIS_KEY_PREFIX + namespace + "::" + cacheKey;
    }

    private List<WmsOptionVo> sanitize(List<WmsOptionVo> options) {
        // TODO【缓存穿透优化扩展点 - 布隆过滤器】
        //
        // 当前方案：
        // - 对查询结果为空的情况，返回空 List，并在 Redis 中使用短 TTL 缓存（见 writeRedis）
        // - 已能有效防止缓存穿透（避免同一不存在条件频繁打 DB）
        //
        // 布隆过滤器说明：
        // - 布隆过滤器适用于“按唯一 ID 查询”的场景（如 userId / orderId）
        // - 本模块为主数据选项查询（status / warehouseId / areaId / keyword 组合条件）
        //   不适合引入布隆过滤器（无法构建稳定 key 集）
        //
        // 何时考虑引入：
        // - 如果后续出现“按单一 ID 查询主数据详情”的高并发接口
        // - 或存在明显恶意 ID 穿透攻击场景
        //
        // 当前结论：
        // - 本模块暂不引入布隆过滤器
        // - 采用“空结果缓存 + 短 TTL”即可
        //
        // 参考：缓存策略 = 穿透（空缓存）+ 击穿（TODO 可加锁）+ 雪崩（TTL 抖动）
        if (options == null || options.isEmpty()) {
            return List.of();
        }
        return Collections.unmodifiableList(options);
    }
}
