package com.telegram.ecommerce.persistence.cache;

import com.telegram.ecommerce.persistence.cache.dto.SignupData;
import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.cacheconfig.CacheConfig;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@Component
public class SignupCacheService {

    private final TedissonCacheManager cacheManager;

    public SignupCacheService(TedissonCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxSize(10000);
        cacheManager.createCache(CacheName.SIGNUP_TOKEN.name(), cacheConfig);
    }

    public SignupData getSignupData(String key) {
        return cacheManager.getItemFromCache(CacheName.SIGNUP_TOKEN.name(), key);
    }

    public void addSignupData(String key, SignupData signupData, long ttlInSeconds) {
        cacheManager.addItemToCache(CacheName.SIGNUP_TOKEN.name(), key, signupData,
                ttlInSeconds, null, TimeUnit.SECONDS);
    }

    public void deleteSignupData(String key) {
        cacheManager.removeItemFromCache(CacheName.SIGNUP_TOKEN.name(), key);
    }
}
