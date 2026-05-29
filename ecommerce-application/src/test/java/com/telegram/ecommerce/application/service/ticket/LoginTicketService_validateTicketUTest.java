package com.telegram.ecommerce.application.service.ticket;

import com.telegram.ecommerce.application.api.exception.InvalidTicketException;
import com.telegram.ecommerce.application.api.exception.TicketValidationBlockException;
import com.telegram.ecommerce.application.config.properties.dto.LoginProperties;
import com.telegram.ecommerce.application.config.properties.dto.TicketProperties;
import com.telegram.ecommerce.application.invoker.sms.SmsService;
import com.telegram.ecommerce.application.util.DateUtil;
import com.telegram.ecommerce.persistence.cache.BlockedMobileNumbersCacheService;
import com.telegram.ecommerce.persistence.cache.LoginTicketCacheService;
import com.telegram.ecommerce.persistence.cache.dto.TicketInfoCacheDto;
import com.telegram.ecommerce.persistence.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginTicketService_validateTicketUTest {

    @Mock
    private DateUtil dateUtil;
    @Mock
    private SmsService smsService;
    @Mock
    private LoginTicketCacheService ticketCacheService;
    @Mock
    private BlockedMobileNumbersCacheService blockedMobileNumbersCacheService;
    @Mock
    private AppUserRepository appUserRepository;

    private LoginTicketService loginTicketService;

    @BeforeEach
    void setUp() {
        LoginProperties loginProperties = new LoginProperties();
        TicketProperties ticketProperties = new TicketProperties();
        ticketProperties.setMaxFailureCount(2);
        ticketProperties.setBlockDuration(Duration.ofMinutes(10));
        loginProperties.setTicket(ticketProperties);
        loginTicketService = new LoginTicketService(dateUtil, smsService, loginProperties, ticketCacheService,
                blockedMobileNumbersCacheService, appUserRepository);
    }

    @Test
    void valid_ticket_deletes_ticket_from_cache_after_validation() throws Exception {
        when(ticketCacheService.getTicketInfoDto("cache-key")).thenReturn(new TicketInfoCacheDto("123456"));

        loginTicketService.validateTicket("cache-key", "123456", "09121111118");

        verify(ticketCacheService).deleteTicket("cache-key", null);
    }

    @Test
    void first_invalid_ticket_increments_failure_count_without_blocking() {
        TicketInfoCacheDto ticketInfoCacheDto = new TicketInfoCacheDto("123456");
        when(ticketCacheService.getTicketInfoDto("cache-key")).thenReturn(ticketInfoCacheDto);

        assertThrows(InvalidTicketException.class,
                () -> loginTicketService.validateTicket("cache-key", "000000", "09121111118"));

        verify(ticketCacheService).updateTicketInfoDto("cache-key", ticketInfoCacheDto);
        verify(blockedMobileNumbersCacheService, never())
                .addMobileNumber(any(), anyLong());
    }

    @Test
    void reaching_max_failure_count_blocks_mobile_number() {
        TicketInfoCacheDto ticketInfoCacheDto = new TicketInfoCacheDto("123456");
        ticketInfoCacheDto.incrementAndGetFailureCount();
        when(ticketCacheService.getTicketInfoDto("cache-key")).thenReturn(ticketInfoCacheDto);

        assertThrows(TicketValidationBlockException.class,
                () -> loginTicketService.validateTicket("cache-key", "000000", "09121111118"));

        verify(ticketCacheService).deleteTicket("cache-key", null);
        verify(blockedMobileNumbersCacheService).addMobileNumber("09121111118", 600L);
    }

    @Test
    void null_ticket_in_cache_throws_invalid_ticket_exception() {
        when(ticketCacheService.getTicketInfoDto("cache-key")).thenReturn(null);

        assertThrows(InvalidTicketException.class,
                () -> loginTicketService.validateTicket("cache-key", "123456", "09121111118"));
    }
}
