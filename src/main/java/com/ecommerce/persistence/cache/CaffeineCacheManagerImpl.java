package com.ecommerce.persistence.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Caffeine (in-memory) cache implementation with per-entry TTL support.
 *
 * @author reza gholamzad
 * @since 6/11/26
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CaffeineCacheManagerImpl implements AppCacheManager {

    private record ExpiringValue<T>(T value, Instant expiresAt) {}

    private final CacheProperties cacheProperties;
    private final ConcurrentMap<String, Cache<String, ExpiringValue<Object>>> caches = new ConcurrentHashMap<>();

    public CaffeineCacheManagerImpl(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    private Cache<String, ExpiringValue<Object>> getOrCreateCache(String cacheName) {
        return caches.computeIfAbsent(cacheName, name ->
                Caffeine.newBuilder()
                        .expireAfter(new Expiry<String, ExpiringValue<Object>>() {
                            @Override
                            public long expireAfterCreate(String key, ExpiringValue<Object> value, long currentTime) {
                                return nanosUntilExpiry(value.expiresAt());
                            }

                            @Override
                            public long expireAfterUpdate(String key, ExpiringValue<Object> value, long currentTime, long currentDuration) {
                                return nanosUntilExpiry(value.expiresAt());
                            }

                            @Override
                            public long expireAfterRead(String key, ExpiringValue<Object> value, long currentTime, long currentDuration) {
                                return currentDuration;
                            }
                        })
                        .maximumSize(cacheProperties.getCaffeine().getMaxSize())
                        .recordStats()
                        .build()
        );
    }

    private long nanosUntilExpiry(Instant expiresAt) {
        long nanos = Instant.now().until(expiresAt, ChronoUnit.NANOS);
        return Math.max(0L, nanos);
    }

    private Instant toExpiry(Duration ttl) {
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            return Instant.now().plus(ttl);
        }
        return Instant.now().plus(cacheProperties.getCaffeine().getTtl());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, String key) {
        Cache<String, ExpiringValue<Object>> cache = getOrCreateCache(cacheName);
        ExpiringValue<Object> wrapper = cache.getIfPresent(key);
        T value = wrapper != null ? (T) wrapper.value() : null;
        log.debug("Cache GET - cache: {}, key: {}, found: {}", cacheName, key, value != null);
        return value;
    }

    @Override
    public <T> void put(String cacheName, String key, T value, Duration ttl) {
        Cache<String, ExpiringValue<Object>> cache = getOrCreateCache(cacheName);
        cache.put(key, new ExpiringValue<>(value, toExpiry(ttl)));
        log.debug("Cache PUT - cache: {}, key: {}, ttl: {}", cacheName, key, ttl);
    }

    @Override
    public void evict(String cacheName, String key) {
        Cache<String, ExpiringValue<Object>> cache = getOrCreateCache(cacheName);
        cache.invalidate(key);
        log.debug("Cache EVICT - cache: {}, key: {}", cacheName, key);
    }

    @Override
    public boolean exists(String cacheName, String key) {
        Cache<String, ExpiringValue<Object>> cache = getOrCreateCache(cacheName);
        return cache.getIfPresent(key) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAndPut(String cacheName, String key, T value, Duration ttl) {
        Cache<String, ExpiringValue<Object>> cache = getOrCreateCache(cacheName);
        ExpiringValue<Object> oldWrapper = cache.getIfPresent(key);
        cache.put(key, new ExpiringValue<>(value, toExpiry(ttl)));
        log.debug("Cache GET_AND_PUT - cache: {}, key: {}, ttl: {}", cacheName, key, ttl);
        return oldWrapper != null ? (T) oldWrapper.value() : null;
    }

    @Override
    public <T> void replace(String cacheName, String key, T value) {
        Cache<String, ExpiringValue<Object>> cache = getOrCreateCache(cacheName);
        ExpiringValue<Object> existing = cache.getIfPresent(key);
        if (existing != null) {
            // Preserve the original expiry — Caffeine's expireAfterUpdate re-reads expiresAt from the new wrapper
            cache.put(key, new ExpiringValue<>(value, existing.expiresAt()));
            log.debug("Cache REPLACE - cache: {}, key: {}", cacheName, key);
        }
    }
}
