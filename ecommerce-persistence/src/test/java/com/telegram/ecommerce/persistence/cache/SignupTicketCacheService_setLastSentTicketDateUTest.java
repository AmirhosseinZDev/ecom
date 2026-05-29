package com.telegram.ecommerce.persistence.cache;

import com.tosan.client.redis.api.TedissonCacheManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignupTicketCacheService_setLastSentTicketDateUTest {

    @Mock
    private TedissonCacheManager cacheManager;

    @InjectMocks
    private SignupTicketCacheService signupTicketCacheService;

    @Test
    void last_sent_ticket_date_is_stored_in_signup_last_ticket_cache() {
        Date lastSentDate = new Date(1_000);
        Date expireDate = new Date(121_000);

        signupTicketCacheService.setLastSentTicketDate("last-sent-key", lastSentDate, expireDate);

        verify(cacheManager).addItemToCache(CacheName.SIGNUP_LAST_TICKET.name(), "last-sent-key", lastSentDate,
                120L, null, TimeUnit.SECONDS);
    }
}
