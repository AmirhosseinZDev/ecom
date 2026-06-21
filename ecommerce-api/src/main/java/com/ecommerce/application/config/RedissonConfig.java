package com.ecommerce.application.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Creates the RedissonClient only when app.cache.type=redis.
 * The Redisson auto-configuration is excluded globally in application.yml to prevent
 * an eager connection attempt when Caffeine is the active cache (the default).
 *
 * @author reza gholamzad
 * @since 6/14/26
 */
@Configuration
@ConditionalOnProperty(name = "app.cache.type", havingValue = "redis")
public class RedissonConfig {

    @Bean
    public RedissonClient redissonClient(
            @Value("${spring.data.redis.host:localhost}") String host,
            @Value("${spring.data.redis.port:6379}") int port) {
        var config = new Config();
        config.useSingleServer().setAddress("redis://" + host + ":" + port);
        return Redisson.create(config);
    }
}
