package com.ecommerce.persistence.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * Caffeine (in-memory) cache implementation.
 *
 * @author reza gholamzad
 * @since 6/11/26
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.cache.type", havingValue = "caffeine", matchIfMissing = true)
public class CaffeineCacheManagerImpl implements AppCacheManager {

    private final CacheProperties cacheProperties;
    private final ConcurrentMap<String, Cache<String, Object>> caches = new ConcurrentHashMap<>();

    public CaffeineCacheManagerImpl(CacheProperties cacheProperties) {
        this.cacheProperties = cacheProperties;
    }

    private Cache<String, Object> getOrCreateCache(String cacheName) {
        return caches.computeIfAbsent(cacheName, name ->
                Caffeine.newBuilder()
                        .expireAfterWrite(cacheProperties.getCaffeine().getTtl().toMillis(), TimeUnit.MILLISECONDS)
                        .maximumSize(cacheProperties.getCaffeine().getMaxSize())
                        .recordStats()
                        .build()
        );
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String cacheName, String key) {
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        T value = (T) cache.getIfPresent(key);
        log.debug("Cache GET - cache: {}, key: {}, found: {}", cacheName, key, value != null);
        return value;
    }

    @Override
    public <T> void put(String cacheName, String key, T value, Duration ttl) {
        // Caffeine cache TTL is set at cache creation, not per entry
        // For per-entry TTL, we'd need a different approach
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        cache.put(key, value);
        log.debug("Cache PUT - cache: {}, key: {}", cacheName, key);
    }

    @Override
    public void evict(String cacheName, String key) {
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        cache.invalidate(key);
        log.debug("Cache EVICT - cache: {}, key: {}", cacheName, key);
    }

    @Override
    public boolean exists(String cacheName, String key) {
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        return cache.getIfPresent(key) != null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAndPut(String cacheName, String key, T value, Duration ttl) {
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        T oldValue = (T) cache.getIfPresent(key);
        cache.put(key, value);
        log.debug("Cache GET_AND_PUT - cache: {}, key: {}", cacheName, key);
        return oldValue;
    }

    @Override
    public <T> void replace(String cacheName, String key, T value) {
        Cache<String, Object> cache = getOrCreateCache(cacheName);
        if (cache.getIfPresent(key) != null) {
            cache.put(key, value);
            log.debug("Cache REPLACE - cache: {}, key: {}", cacheName, key);
        }
    }
}
