package com.ecommerce.persistence.cache;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * @author reza gholamzad
 * @since 6/11/26
 */
@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.cache")
public class CacheProperties {

    /**
     * Cache type: caffeine or redis
     */
    private CacheType type = CacheType.caffeine;

    private CaffeineProperties caffeine = new CaffeineProperties();

    @Getter
    @Setter
    public static class CaffeineProperties {
        private Duration ttl = Duration.ofMinutes(10);
        private long maxSize = 10000;
    }

    public enum CacheType {
        caffeine,
        redis
    }
}
