package com.ecommerce.persistence.cache;

import org.springframework.stereotype.Component;

/**
 * @author AmirHossein ZamanZade
 * @since 5/29/26
 */
@Component
public class LoginTicketCacheService extends AbstractTicketCacheService {

    public LoginTicketCacheService(AppCacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected CacheName getTicketCacheName() {
        return CacheName.LOGIN_TICKET;
    }

    @Override
    protected CacheName getLastSentCacheName() {
        return CacheName.LOGIN_LAST_TICKET;
    }
}
