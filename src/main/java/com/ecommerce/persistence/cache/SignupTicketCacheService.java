package com.ecommerce.persistence.cache;

import org.springframework.stereotype.Component;

/**
 * @author AmirHossein ZamanZade
 * @since 5/29/26
 */
@Component
public class SignupTicketCacheService extends AbstractTicketCacheService {

    public SignupTicketCacheService(AppCacheManager cacheManager) {
        super(cacheManager);
    }

    @Override
    protected CacheName getTicketCacheName() {
        return CacheName.SIGNUP_TICKET;
    }

    @Override
    protected CacheName getLastSentCacheName() {
        return CacheName.SIGNUP_LAST_TICKET;
    }
}
