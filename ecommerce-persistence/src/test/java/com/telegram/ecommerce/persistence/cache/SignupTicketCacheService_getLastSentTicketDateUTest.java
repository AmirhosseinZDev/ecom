package com.telegram.ecommerce.persistence.cache;

import com.tosan.client.redis.api.TedissonCacheManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SignupTicketCacheService_getLastSentTicketDateUTest {

    @Mock
    private TedissonCacheManager cacheManager;

    @InjectMocks
    private SignupTicketCacheService signupTicketCacheService;

    @Test
    void last_sent_ticket_date_is_read_from_signup_last_ticket_cache() {
        Date lastSentDate = new Date();
        when(cacheManager.getItemFromCache(CacheName.SIGNUP_LAST_TICKET.name(), "last-sent-key"))
                .thenReturn(lastSentDate);

        Date result = signupTicketCacheService.getLastSentTicketDate("last-sent-key");

        assertSame(lastSentDate, result);
        verify(cacheManager).getItemFromCache(CacheName.SIGNUP_LAST_TICKET.name(), "last-sent-key");
    }
}
