package com.ecommerce.persistence.cache;

import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author AmirHossein ZamanZade
 * @since 11/18/2023
 */
@Component
public class BlockedMobileNumbersCacheService {

    private static final String CACHE_NAME = CacheName.BLOCKED_MOBILE_NUMBER.name();

    private final AppCacheManager cacheManager;

    public BlockedMobileNumbersCacheService(AppCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public boolean isMobileNumberExistInBlockedMobileNumbers(String key) {
        return cacheManager.exists(CACHE_NAME, key);
    }

    public void addMobileNumber(String key, long blockDurationSeconds) {
        cacheManager.put(CACHE_NAME, key, "blocked", Duration.ofSeconds(blockDurationSeconds));
    }
}
