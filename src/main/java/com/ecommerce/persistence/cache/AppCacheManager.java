package com.ecommerce.persistence.cache;

import java.time.Duration;

/**
 * Abstraction for cache operations that works with both Caffeine and Redis.
 *
 * @author reza gholamzad
 * @since 6/11/26
 */
public interface AppCacheManager {

    <T> T get(String cacheName, String key);

    <T> void put(String cacheName, String key, T value, Duration ttl);

    void evict(String cacheName, String key);

    boolean exists(String cacheName, String key);

    <T> T getAndPut(String cacheName, String key, T value, Duration ttl);
}
