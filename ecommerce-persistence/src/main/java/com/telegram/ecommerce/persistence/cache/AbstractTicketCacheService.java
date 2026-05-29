package com.telegram.ecommerce.persistence.cache;

import com.telegram.ecommerce.persistence.cache.dto.TicketInfoCacheDto;
import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.cacheconfig.CacheConfig;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Base class for OTP-ticket cache services.
 * Subclasses declare which cache buckets to use via {@link #getTicketCacheName()}
 * and {@link #getLastSentCacheName()}, so adding a new ticket flow requires
 * only a new {@link CacheName} entry and a concrete subclass.
 *
 * @author AmirHossein ZamanZade
 * @since 5/29/26
 */
public abstract class AbstractTicketCacheService {

    protected final TedissonCacheManager cacheManager;

    protected AbstractTicketCacheService(TedissonCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxSize(10000);
        cacheManager.createCache(getTicketCacheName().name(), cacheConfig);
        cacheManager.createCache(getLastSentCacheName().name(), cacheConfig);
    }

    protected abstract CacheName getTicketCacheName();

    protected abstract CacheName getLastSentCacheName();

    public TicketInfoCacheDto getTicketInfoDto(String key) {
        return cacheManager.getItemFromCache(getTicketCacheName().name(), key);
    }

    public void addTicket(String key, Long userId, TicketInfoCacheDto ticketInfoCacheDto, Date expirationDate) {
        cacheManager.addItemToCache(getTicketCacheName().name(), key, ticketInfoCacheDto,
                getDateDiffInSeconds(expirationDate, new Date()), null, TimeUnit.SECONDS);
        if (userId != null) {
            cacheManager.addItemToCache(getLastSentCacheName().name(), userId.toString(), ticketInfoCacheDto,
                    getDateDiffInSeconds(expirationDate, new Date()), null, TimeUnit.SECONDS);
        }
    }

    public void deleteTicket(String key, Long userId) {
        cacheManager.removeItemFromCache(getTicketCacheName().name(), key);
        if (userId != null) {
            cacheManager.removeItemFromCache(getLastSentCacheName().name(), userId.toString());
        }
    }

    public void setLastSentTicketDate(String key, Date lastSentDate, Date expireDate) {
        cacheManager.addItemToCache(getLastSentCacheName().name(), key, lastSentDate,
                getDateDiffInSeconds(lastSentDate, expireDate), null, TimeUnit.SECONDS);
    }

    public Date getLastSentTicketDate(String key) {
        return cacheManager.getItemFromCache(getLastSentCacheName().name(), key);
    }

    public void deleteLastSentTicketDate(String key) {
        cacheManager.removeItemFromCache(getLastSentCacheName().name(), key);
    }

    public void updateTicketInfoDto(String key, TicketInfoCacheDto ticketInfoCacheDto) {
        cacheManager.replaceCacheItem(getTicketCacheName().name(), key, ticketInfoCacheDto);
    }

    public long getDateDiffInSeconds(Date from, Date to) {
        return getDateDiffInMillis(from, to) / 1000;
    }

    public long getDateDiffInMillis(Date from, Date to) {
        return Math.abs(to.getTime() - from.getTime());
    }
}
