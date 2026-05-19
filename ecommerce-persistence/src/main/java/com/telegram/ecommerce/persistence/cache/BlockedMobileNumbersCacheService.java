package com.telegram.ecommerce.persistence.cache;

import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.cacheconfig.CacheConfig;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author AmirHossein ZamanZade
 * @since 11/18/2023
 */
@Component
public class BlockedMobileNumbersCacheService {

    private final TedissonCacheManager cacheManager;

    public BlockedMobileNumbersCacheService(TedissonCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxSize(10000);
        cacheManager.createCache(CacheName.BLOCKED_MOBILE_NUMBER.name(), cacheConfig);
    }

    public boolean isMobileNumberExistInBlockedMobileNumbers(String key) {
        return cacheManager.isKeyInCache(CacheName.BLOCKED_MOBILE_NUMBER.name(), key);
    }

    public void addMobileNumber(String key, long blockDuration) {
        cacheManager.addItemToCache(CacheName.BLOCKED_MOBILE_NUMBER.name(), key, "", blockDuration, null
                , TimeUnit.SECONDS);
    }
}
