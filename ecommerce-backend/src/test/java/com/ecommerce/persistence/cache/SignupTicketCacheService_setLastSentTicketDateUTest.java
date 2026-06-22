package com.ecommerce.persistence.cache;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SignupTicketCacheService_setLastSentTicketDateUTest {

    @Mock
    private AppCacheManager cacheManager;

    @InjectMocks
    private SignupTicketCacheService signupTicketCacheService;

    @Test
    void last_sent_ticket_date_is_stored_in_signup_last_ticket_cache() {
        Date lastSentDate = new Date(1_000);
        Date expireDate = new Date(121_000);

        signupTicketCacheService.setLastSentTicketDate("last-sent-key", lastSentDate, expireDate);

        verify(cacheManager).put(eq(CacheName.SIGNUP_LAST_TICKET.name()), eq("last-sent-key"), eq(lastSentDate),
                any(Duration.class));
    }
}
