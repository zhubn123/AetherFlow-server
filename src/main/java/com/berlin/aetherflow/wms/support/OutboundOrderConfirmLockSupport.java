package com.berlin.aetherflow.wms.support;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 出库单确认短锁：Redis 可用时优先走分布式锁，不可用时降级为单机本地锁。
 */
@Slf4j
@Component
public class OutboundOrderConfirmLockSupport {

    private static final String KEY_PREFIX = "aetherflow:wms:outbound:confirm:";
    private static final String DEBUG_KEY_PREFIX = "aetherflow:wms:debug:outbound:confirm:";
    private static final Duration DEBUG_KEY_TTL = Duration.ofMinutes(30);
    private static final String TRACE_LAST_ACQUIRE = "last-acquire";
    private static final String TRACE_LAST_RELEASE = "last-release";
    private static final String TRACE_LAST_LOCAL_LOCK = "last-local-lock";
    private static final DefaultRedisScript<Long> RELEASE_LOCK_SCRIPT = new DefaultRedisScript<>(
            """
            if redis.call('get', KEYS[1]) == ARGV[1] then
                return redis.call('del', KEYS[1])
            end
            return 0
            """,
            Long.class
    );

    private final ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider;
    private final Duration redisLockTtl;
    private final Duration redisFailureBackoff;
    private final ConcurrentMap<Long, ReentrantLock> localLocks = new ConcurrentHashMap<>();
    private final AtomicReference<Instant> redisRetryAfter = new AtomicReference<>(Instant.EPOCH);

    public OutboundOrderConfirmLockSupport(
            ObjectProvider<StringRedisTemplate> stringRedisTemplateProvider,
            @Value("${aether-flow.wms.outbound-confirm-lock.ttl:15s}") Duration redisLockTtl,
            @Value("${aether-flow.wms.outbound-confirm-lock.redis-failure-backoff:30s}") Duration redisFailureBackoff
    ) {
        this.stringRedisTemplateProvider = stringRedisTemplateProvider;
        this.redisLockTtl = redisLockTtl;
        this.redisFailureBackoff = redisFailureBackoff;
    }

    public LockHandle acquire(Long orderId) {
        if (orderId == null) {
            throw new RuntimeException("出库单ID不能为空");
        }

        String redisKey = KEY_PREFIX + orderId;
        String ownerToken = UUID.randomUUID().toString();
        RedisLockAcquireResult redisResult = tryAcquireRedisLock(redisKey, ownerToken);
        if (redisResult == RedisLockAcquireResult.ACQUIRED) {
            log.info("A008 debug - acquired redis outbound confirm lock, orderId={}, key={}", orderId, redisKey);
            writeDebugTrace(TRACE_LAST_ACQUIRE,
                    "result=ACQUIRED,orderId=" + orderId + ",key=" + redisKey
                            + ",ownerToken=" + ownerToken + ",time=" + LocalDateTime.now());
            return new LockHandle(() -> releaseRedisLock(redisKey, ownerToken));
        }
        if (redisResult == RedisLockAcquireResult.BUSY) {
            log.info("A008 debug - outbound confirm lock busy, orderId={}, key={}", orderId, redisKey);
            writeDebugTrace(TRACE_LAST_ACQUIRE,
                    "result=BUSY,orderId=" + orderId + ",key=" + redisKey
                            + ",ownerToken=" + ownerToken + ",time=" + LocalDateTime.now());
            throw new RuntimeException("出库单正在确认处理中，请勿重复提交");
        }

        log.info("A008 debug - redis lock unavailable, fallback to local lock, orderId={}, key={}", orderId, redisKey);
        writeDebugTrace(TRACE_LAST_ACQUIRE,
                "result=UNAVAILABLE,orderId=" + orderId + ",key=" + redisKey
                        + ",ownerToken=" + ownerToken + ",time=" + LocalDateTime.now());
        ReentrantLock localLock = localLocks.computeIfAbsent(orderId, key -> new ReentrantLock());
        if (!localLock.tryLock()) {
            writeDebugTrace(TRACE_LAST_LOCAL_LOCK,
                    "result=BUSY,orderId=" + orderId + ",time=" + LocalDateTime.now());
            throw new RuntimeException("出库单正在确认处理中，请勿重复提交");
        }
        writeDebugTrace(TRACE_LAST_LOCAL_LOCK,
                "result=ACQUIRED,orderId=" + orderId + ",time=" + LocalDateTime.now());
        return new LockHandle(() -> releaseLocalLock(orderId, localLock));
    }

    private RedisLockAcquireResult tryAcquireRedisLock(String redisKey, String ownerToken) {
        StringRedisTemplate stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (stringRedisTemplate == null) {
            return RedisLockAcquireResult.UNAVAILABLE;
        }
        Instant retryAfter = redisRetryAfter.get();
        if (retryAfter.isAfter(Instant.now())) {
            // Redis 刚失败过，短时间内直接走本地锁，避免每次请求都重复打连接失败日志。
            return RedisLockAcquireResult.UNAVAILABLE;
        }
        try {
            Boolean acquired = stringRedisTemplate.opsForValue().setIfAbsent(redisKey, ownerToken, redisLockTtl);
            redisRetryAfter.set(Instant.EPOCH);
            return Boolean.TRUE.equals(acquired) ? RedisLockAcquireResult.ACQUIRED : RedisLockAcquireResult.BUSY;
        } catch (Exception ex) {
            Instant nextRetryAt = Instant.now().plus(redisFailureBackoff);
            redisRetryAfter.set(nextRetryAt);
            log.warn("Redis 出库确认锁不可用，降级到本地锁，key={}, reason={}", redisKey, ex.getMessage());
            log.warn("Redis 出库确认锁将在 {} 后重试连接", redisFailureBackoff);
            log.debug("Redis 出库确认锁连接异常明细，key={}", redisKey, ex);
            return RedisLockAcquireResult.UNAVAILABLE;
        }
    }

    private void releaseRedisLock(String redisKey, String ownerToken) {
        StringRedisTemplate stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            stringRedisTemplate.execute(RELEASE_LOCK_SCRIPT, List.of(redisKey), ownerToken);
            log.info("A008 debug - released redis outbound confirm lock, key={}", redisKey);
            writeDebugTrace(TRACE_LAST_RELEASE,
                    "result=RELEASED,key=" + redisKey + ",ownerToken=" + ownerToken
                            + ",time=" + LocalDateTime.now());
        } catch (Exception ex) {
            log.warn("Redis 出库确认锁释放失败，key={}", redisKey, ex);
        }
    }

    private void releaseLocalLock(Long orderId, ReentrantLock localLock) {
        try {
            localLock.unlock();
            writeDebugTrace(TRACE_LAST_LOCAL_LOCK,
                    "result=RELEASED,orderId=" + orderId + ",time=" + LocalDateTime.now());
        } finally {
            if (!localLock.isLocked() && !localLock.hasQueuedThreads()) {
                localLocks.remove(orderId, localLock);
            }
        }
    }

    private void writeDebugTrace(String suffix, String value) {
        StringRedisTemplate stringRedisTemplate = stringRedisTemplateProvider.getIfAvailable();
        if (stringRedisTemplate == null) {
            return;
        }
        try {
            stringRedisTemplate.opsForValue().set(DEBUG_KEY_PREFIX + suffix, value, DEBUG_KEY_TTL);
        } catch (Exception ex) {
            log.debug("A008 debug - failed to write debug trace, suffix={}", suffix, ex);
        }
    }

    public static final class LockHandle implements AutoCloseable {

        private final Runnable releaseAction;
        private boolean released;

        private LockHandle(Runnable releaseAction) {
            this.releaseAction = releaseAction;
        }

        @Override
        public void close() {
            if (released) {
                return;
            }
            releaseAction.run();
            released = true;
        }
    }

    private enum RedisLockAcquireResult {
        ACQUIRED,
        BUSY,
        UNAVAILABLE
    }
}
