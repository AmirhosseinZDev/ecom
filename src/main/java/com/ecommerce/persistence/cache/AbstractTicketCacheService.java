package com.ecommerce.persistence.cache;

import com.ecommerce.persistence.cache.dto.TicketInfoCacheDto;

import java.time.Duration;
import java.util.Date;

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

    protected final AppCacheManager cacheManager;

    protected AbstractTicketCacheService(AppCacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    protected abstract CacheName getTicketCacheName();

    protected abstract CacheName getLastSentCacheName();

    public TicketInfoCacheDto getTicketInfoDto(String key) {
        return cacheManager.get(getTicketCacheName().name(), key);
    }

    public void addTicket(String key, Long userId, TicketInfoCacheDto ticketInfoCacheDto, Date expirationDate) {
        Duration ttl = getDurationUntil(expirationDate);

        cacheManager.put(getTicketCacheName().name(), key, ticketInfoCacheDto, ttl);

        if (userId != null) {
            cacheManager.put(getLastSentCacheName().name(), userId.toString(), ticketInfoCacheDto, ttl);
        }
    }

    public void deleteTicket(String key, Long userId) {
        cacheManager.evict(getTicketCacheName().name(), key);

        if (userId != null) {
            cacheManager.evict(getLastSentCacheName().name(), userId.toString());
        }
    }

    public void setLastSentTicketDate(String key, Date lastSentDate, Date expireDate) {
        Duration ttl = Duration.ofMillis(Math.abs(expireDate.getTime() - lastSentDate.getTime()));
        cacheManager.put(getLastSentCacheName().name(), key, lastSentDate, ttl);
    }

    public Date getLastSentTicketDate(String key) {
        return cacheManager.get(getLastSentCacheName().name(), key);
    }

    public void deleteLastSentTicketDate(String key) {
        cacheManager.evict(getLastSentCacheName().name(), key);
    }

    public void updateTicketInfoDto(String key, TicketInfoCacheDto ticketInfoCacheDto) {
        cacheManager.replace(getTicketCacheName().name(), key, ticketInfoCacheDto);
    }

    public Duration getDurationUntil(Date expirationDate) {
        long diffMillis = Math.abs(expirationDate.getTime() - System.currentTimeMillis());
        return Duration.ofMillis(diffMillis);
    }

    public long getDateDiffInSeconds(Date from, Date to) {
        return Math.abs(to.getTime() - from.getTime()) / 1000;
    }

    public long getDateDiffInMillis(Date from, Date to) {
        return Math.abs(to.getTime() - from.getTime());
    }
}
