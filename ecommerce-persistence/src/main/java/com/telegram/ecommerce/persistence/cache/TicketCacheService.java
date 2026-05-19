package com.telegram.ecommerce.persistence.cache;

import com.telegram.ecommerce.persistence.cache.dto.TicketInfoCacheDto;
import com.tosan.client.redis.api.TedissonCacheManager;
import com.tosan.client.redis.cacheconfig.CacheConfig;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@Component
public class TicketCacheService {
    private final TedissonCacheManager cacheManager;

    public TicketCacheService(TedissonCacheManager cacheManager) {
        this.cacheManager = cacheManager;
        CacheConfig cacheConfig = new CacheConfig();
        cacheConfig.setMaxSize(10000);
        cacheManager.createCache(CacheName.SIGNUP_TICKET.name(), cacheConfig);
        cacheManager.createCache(CacheName.USER_LAST_TICKET.name(), cacheConfig);
    }

    public TicketInfoCacheDto getTicketInfoDto(String key) {
        return cacheManager.getItemFromCache(CacheName.SIGNUP_TICKET.name(), key);
    }

    public void addTicket(String key, Long userId, TicketInfoCacheDto ticketInfoCacheDto, Date expirationDate) {
        cacheManager.addItemToCache(CacheName.SIGNUP_TICKET.name(), key, ticketInfoCacheDto,
                getDateDiffInSeconds(expirationDate, new Date()), null, TimeUnit.SECONDS);
        if (userId != null) {
            cacheManager.addItemToCache(CacheName.USER_LAST_TICKET.name(), userId.toString(), ticketInfoCacheDto,
                    getDateDiffInSeconds(expirationDate, new Date()), null, TimeUnit.SECONDS);
        }
    }

    public void deleteTicket(String key, Long userId) {
        cacheManager.removeItemFromCache(CacheName.SIGNUP_TICKET.name(), key);
        if (userId != null) {
            cacheManager.removeItemFromCache(CacheName.USER_LAST_TICKET.name(), userId.toString());
        }
    }

    public void setLastSentTicketDate(String key, Date lastSentDate, Date expireDate) {
        cacheManager.addItemToCache(CacheName.USER_LAST_TICKET.name(), key, lastSentDate,
                getDateDiffInSeconds(lastSentDate, expireDate), null, TimeUnit.SECONDS);
    }

    public Date getLastSentTicketDate(String key) {
        return cacheManager.getItemFromCache(CacheName.USER_LAST_TICKET.name(), key);
    }

    public void deleteLastSentTicketDate(String key) {
        cacheManager.removeItemFromCache(CacheName.USER_LAST_TICKET.name(), key);
    }

    public void updateTicketInfoDto(String key, TicketInfoCacheDto ticketInfoCacheDto) {
        cacheManager.replaceCacheItem(CacheName.SIGNUP_TICKET.name(), key, ticketInfoCacheDto);
    }

    public long getDateDiffInSeconds(Date from, Date to) {
        return getDateDiffInMillis(from, to) / 1000;
    }

    public long getDateDiffInMillis(Date from, Date to) {
        return Math.abs(to.getTime() - from.getTime());
    }

}
