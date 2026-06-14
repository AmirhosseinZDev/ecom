package com.ecommerce.persistence.cache;

import com.ecommerce.persistence.cache.dto.SignupData;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@Component
public class SignupCacheService {

    private static final String CACHE_NAME = CacheName.SIGNUP_TOKEN.name();

    private final AppCacheManager cacheManager;

    public SignupCacheService(AppCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public SignupData getSignupData(String key) {
        return cacheManager.get(CACHE_NAME, key);
    }

    public void addSignupData(String key, SignupData signupData, long ttlInSeconds) {
        cacheManager.put(CACHE_NAME, key, signupData, Duration.ofSeconds(ttlInSeconds));
    }

    public void deleteSignupData(String key) {
        cacheManager.evict(CACHE_NAME, key);
    }
}
