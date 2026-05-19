package com.telegram.ecommerce.application.service.ticket;

import com.telegram.ecommerce.application.api.exception.InvalidTicketException;
import com.telegram.ecommerce.application.api.exception.TicketValidationBlockException;
import com.telegram.ecommerce.application.config.properties.dto.SignupProperties;
import com.telegram.ecommerce.application.config.properties.dto.TicketProperties;
import com.telegram.ecommerce.application.invoker.sms.SmsService;
import com.telegram.ecommerce.application.util.DateUtil;
import com.telegram.ecommerce.persistence.cache.BlockedMobileNumbersCacheService;
import com.telegram.ecommerce.persistence.cache.TicketCacheService;
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
class SignupTicketService_validateTicketUTest {

    @Mock
    private DateUtil dateUtil;
    @Mock
    private SmsService smsService;
    @Mock
    private TicketCacheService ticketCacheService;
    @Mock
    private BlockedMobileNumbersCacheService blockedMobileNumbersCacheService;
    @Mock
    private AppUserRepository appUserRepository;

    private SignupTicketService signupTicketService;

    @BeforeEach
    void setUp() {
        SignupProperties signupProperties = new SignupProperties();
        TicketProperties ticketProperties = new TicketProperties();
        ticketProperties.setMaxFailureCount(2);
        ticketProperties.setBlockDuration(Duration.ofMinutes(5));
        signupProperties.setTicket(ticketProperties);
        signupTicketService = new SignupTicketService(dateUtil, smsService, signupProperties, ticketCacheService,
                blockedMobileNumbersCacheService, appUserRepository);
    }

    @Test
    void valid_ticket_deletes_ticket_after_validation() throws Exception {
        when(ticketCacheService.getTicketInfoDto("cache-key")).thenReturn(new TicketInfoCacheDto("1234"));

        signupTicketService.validateTicket("cache-key", "1234", "09121111118");

        verify(ticketCacheService).deleteTicket("cache-key", null);
    }

    @Test
    void first_invalid_ticket_increments_failure_count() {
        TicketInfoCacheDto ticketInfoCacheDto = new TicketInfoCacheDto("1234");
        when(ticketCacheService.getTicketInfoDto("cache-key")).thenReturn(ticketInfoCacheDto);

        assertThrows(InvalidTicketException.class,
                () -> signupTicketService.validateTicket("cache-key", "0000", "09121111118"));

        verify(ticketCacheService).updateTicketInfoDto("cache-key", ticketInfoCacheDto);
        verify(blockedMobileNumbersCacheService, never()).addMobileNumber(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong());
    }

    @Test
    void max_invalid_ticket_attempts_blocks_mobile_number() {
        TicketInfoCacheDto ticketInfoCacheDto = new TicketInfoCacheDto("1234");
        ticketInfoCacheDto.incrementAndGetFailureCount();
        when(ticketCacheService.getTicketInfoDto("cache-key")).thenReturn(ticketInfoCacheDto);

        assertThrows(TicketValidationBlockException.class,
                () -> signupTicketService.validateTicket("cache-key", "0000", "09121111118"));

        verify(ticketCacheService).deleteTicket("cache-key", null);
        verify(blockedMobileNumbersCacheService).addMobileNumber("09121111118", 300L);
    }
}
