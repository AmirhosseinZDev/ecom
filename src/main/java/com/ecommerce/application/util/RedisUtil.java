package com.ecommerce.application.util;

import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.api.ratelimiter.RateLimiterArgs;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.TemporalUnit;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.function.Supplier;

/**
 * @author reza gholamzad
 * @since 6/11/26
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class RedisUtil {

    private static final String ECOM_TIME_MAP = "PC-time-map";
    private static final String ECOM_RATE_LIMIT = "PC_RL_";
    private static final String ECOM_SEMAPHORE = "PC_SM_";
    private static final String ECOM_LOCK = "PC_LOCK_";
    private final RedissonClient client;

    public void checkRateLimit(String key, long rate, long intervalMinutes) {

        var rateLimiterKey = ECOM_RATE_LIMIT.concat(key);
        var rateLimiter = client.getRateLimiter(rateLimiterKey);

        var interval = Duration.ofMinutes(intervalMinutes);
        var isNewRateLimiter = rateLimiter.trySetRate(RateType.OVERALL, rate, interval);

        if (!isNewRateLimiter) {

            var currentConfig = rateLimiter.getConfig();
            var isConfigurationChanged = !currentConfig.getRate().equals(rate) ||
                    !currentConfig.getRateInterval().equals(interval.toMillis());

            if (isConfigurationChanged) {
                log.info("Updating rate limiter configuration - rate: {}, interval: {}", rate, intervalMinutes);
                rateLimiter.setRate(RateLimiterArgs.of(RateType.OVERALL, rate, interval));
            }
        }

        if (!rateLimiter.tryAcquire()) {
            log.error("Rate limit exceeded for key: {}", key);
            throw new EcommerceException(ECOMErrorType.TOO_MANY_REQUEST);
        }
    }

    public void tryLock(String id) {
        if (!getNativeLock(id).tryLock()) {
            throw new EcommerceException(ECOMErrorType.TOO_MANY_REQUEST);
        }
    }

    public void unlock(String id) {
        getNativeLock(id).unlock();
    }

    public void tryAcquireSemaphore(String key, int permits, long leaseTime) {

        var semaphoreKey = ECOM_SEMAPHORE.concat(key);
        var semaphore = client.getSemaphore(semaphoreKey);

        var leaseDuration = Duration.ofMinutes(leaseTime);
        semaphore.trySetPermits(permits, leaseDuration);

        if (!semaphore.tryAcquire()) {
            log.error("Semaphore limit exceeded for key: {}", key);
            throw new EcommerceException(ECOMErrorType.TOO_MANY_REQUEST);
        }
    }

    public void releaseSemaphore(String key) {
        var semaphoreKey = ECOM_TIME_MAP.concat(key);
        var semaphore = client.getSemaphore(semaphoreKey);
        semaphore.release();
    }

    public void deleteSemaphore(String key) {
        var semaphoreKey = ECOM_TIME_MAP.concat(key);
        var semaphore = client.getSemaphore(semaphoreKey);
        semaphore.delete();
    }

    public RedissonClient getClient() {
        return client;
    }

    public Lock getLock(String id) {
        return client.getLock(ECOM_LOCK.concat(id));
    }

    public RLock getNativeLock(String id) {
        return client.getLock(ECOM_LOCK.concat(id));
    }

    public RLock getNativeFairLock(String id) {
        return client.getFairLock(ECOM_LOCK.concat(id));
    }

    public void doWithLock(String lockName, long waitTimeMillis, long leaseTimeMillis, Runnable func) {
        try {
            if (getNativeLock(lockName).tryLock(waitTimeMillis, leaseTimeMillis, TimeUnit.MILLISECONDS)) {
                try {
                    func.run();
                } finally {
                    getNativeLock(lockName).unlock();
                }
            } else {
                log.error("Timeout in getting redis lock, lockName: {}", lockName);
                throw new EcommerceException(ECOMErrorType.GENERAL_ERROR, "Timeout in getting redis lock");
            }
        } catch (InterruptedException e) {
            log.error("error in getting lock, lockName:{}", lockName, e);
            Thread.currentThread().interrupt(); //restore the interrupt flag
            throw new EcommerceException(ECOMErrorType.GENERAL_ERROR, "InterruptedException in redis lock");
        }
    }

    public <R> R doWithLock(String lockName, long waitTimeMillis, long leaseTimeMillis, Supplier<R> func) {
        try {
            if (getNativeLock(lockName).tryLock(waitTimeMillis, leaseTimeMillis, TimeUnit.MILLISECONDS)) {
                try {
                    return func.get();
                } finally {
                    getNativeLock(lockName).unlock();
                }
            } else {
                log.error("Timeout in getting redis lock, lockName: {}", lockName);
                throw new EcommerceException(ECOMErrorType.GENERAL_ERROR, "Timeout in getting redis lock");
            }
        } catch (InterruptedException e) {
            log.error("error in getting lock, lockName:{}", lockName, e);
            Thread.currentThread().interrupt(); //restore the interrupt flag
            throw new EcommerceException(ECOMErrorType.GENERAL_ERROR, "InterruptedException in redis lock");
        }
    }

    public void doAfterTime(String key, long time, TemporalUnit unit, Runnable func) {
        RMap<String, LocalDateTime> timeMap = client.getMap(ECOM_TIME_MAP);
        var localDateTime = timeMap.get(key);
        if (localDateTime == null || localDateTime.plus(time, unit).isBefore(LocalDateTime.now())) {
            func.run();
            timeMap.put(key, LocalDateTime.now());
        } else {
            log.info("for scheduler key {} time is not reached yet!", key);
        }
    }

}
