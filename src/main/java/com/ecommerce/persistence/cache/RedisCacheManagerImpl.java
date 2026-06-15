package com.ecommerce.persistence.cache;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Redis (Redisson) cache implementation.
 *
 * @author reza gholamzad
 * @since 6/11/26
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.cache.type", havingValue = "redis")
public class RedisCacheManagerImpl implements AppCacheManager {

    private final RedissonClient redissonClient;

    public RedisCacheManagerImpl(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    private String buildKey(String cacheName, String key) {
        return cacheName + ":" + key;
    }

    @Override
    public <T> T get(String cacheName, String key) {
        String fullKey = buildKey(cacheName, key);
        RBucket<T> bucket = redissonClient.getBucket(fullKey);
        T value = bucket.get();
        log.debug("Cache GET - cache: {}, key: {}, found: {}", cacheName, key, value != null);
        return value;
    }

    @Override
    public <T> void put(String cacheName, String key, T value, Duration ttl) {
        String fullKey = buildKey(cacheName, key);
        RBucket<T> bucket = redissonClient.getBucket(fullKey);
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            bucket.set(value, ttl);
        } else {
            bucket.set(value);
        }
        log.debug("Cache PUT - cache: {}, key: {}, ttl: {}", cacheName, key, ttl);
    }

    @Override
    public void evict(String cacheName, String key) {
        String fullKey = buildKey(cacheName, key);
        redissonClient.getBucket(fullKey).delete();
        log.debug("Cache EVICT - cache: {}, key: {}", cacheName, key);
    }

    @Override
    public boolean exists(String cacheName, String key) {
        String fullKey = buildKey(cacheName, key);
        return redissonClient.getBucket(fullKey).isExists();
    }

    @Override
    public <T> T getAndPut(String cacheName, String key, T value, Duration ttl) {
        String fullKey = buildKey(cacheName, key);
        RBucket<T> bucket = redissonClient.getBucket(fullKey);
        T oldValue = bucket.get();
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            bucket.set(value, ttl);
        } else {
            bucket.set(value);
        }
        log.debug("Cache GET_AND_PUT - cache: {}, key: {}, ttl: {}", cacheName, key, ttl);
        return oldValue;
    }

    @Override
    public <T> void replace(String cacheName, String key, T value) {
        String fullKey = buildKey(cacheName, key);
        RBucket<T> bucket = redissonClient.getBucket(fullKey);
        long remainMs = bucket.remainTimeToLive();
        if (remainMs > 0) {
            bucket.set(value, Duration.ofMillis(remainMs));
        } else if (remainMs == -1) {
            // Key exists but has no TTL — update value without setting one
            bucket.set(value);
        }
        // remainMs == -2 means key does not exist — no-op
        log.debug("Cache REPLACE - cache: {}, key: {}, remainMs: {}", cacheName, key, remainMs);
    }
}
