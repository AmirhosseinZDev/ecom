package com.ecommerce.application.service.user;

import com.ecommerce.application.api.dto.user.SendSignupTicketRequestDto;
import com.ecommerce.application.api.dto.user.SendSignupTicketResponseDto;
import com.ecommerce.application.api.exception.TicketValidationBlockException;
import com.ecommerce.application.config.properties.dto.LoginProperties;
import com.ecommerce.application.config.properties.dto.SignupProperties;
import com.ecommerce.application.config.properties.dto.TicketProperties;
import com.ecommerce.application.service.jwt.JwtService;
import com.ecommerce.application.service.ticket.LoginTicketService;
import com.ecommerce.application.service.ticket.SignupTicketService;
import com.ecommerce.application.service.ticket.dto.TicketGenerateRequestDto;
import com.ecommerce.persistence.cache.SignupCacheService;
import com.ecommerce.persistence.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserService_sendSignupTicketUTest {

    @Mock
    private AppUserRepository appUserRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private SignupTicketService signupTicketService;
    @Mock
    private LoginTicketService loginTicketService;
    @Mock
    private SignupCacheService signupCacheService;

    private UserService userService;

    @BeforeEach
    void setUp() {
        SignupProperties signupProperties = new SignupProperties();
        TicketProperties ticketProperties = new TicketProperties();
        ticketProperties.setTemplateId(100001);
        ticketProperties.setTimeToLive(Duration.ofMinutes(2));
        signupProperties.setTicket(ticketProperties);
        signupProperties.setTokenTtl(Duration.ofMinutes(10));
        userService = new UserService(appUserRepository, passwordEncoder, authenticationManager,
                jwtService, signupTicketService, loginTicketService, signupCacheService,
                signupProperties, new LoginProperties());
    }

    @Test
    void sends_ticket_and_returns_ttl_in_seconds() throws Exception {
        SendSignupTicketRequestDto requestDto = new SendSignupTicketRequestDto();
        requestDto.setMobileNumber("09121111118");

        SendSignupTicketResponseDto responseDto = userService.sendSignupTicket(requestDto);

        assertEquals(120L, responseDto.getTicketTTLInSecond());
        ArgumentCaptor<TicketGenerateRequestDto> captor = ArgumentCaptor.forClass(TicketGenerateRequestDto.class);
        verify(signupTicketService).sendTicket(captor.capture());
        TicketGenerateRequestDto sent = captor.getValue();
        assertEquals("09121111118", sent.getMobileNumber());
        assertEquals("09121111118", sent.getCacheKey());
        assertEquals("09121111118", sent.getLastSentCacheKey());
        assertEquals(100001, sent.getSmsTemplateId());
        assertEquals(120, sent.getTicketTimeToLive());
    }

    @Test
    void block_exception_is_propagated() throws Exception {
        SendSignupTicketRequestDto requestDto = new SendSignupTicketRequestDto();
        requestDto.setMobileNumber("09121111118");
        doThrow(new TicketValidationBlockException("blocked"))
                .when(signupTicketService).sendTicket(any());

        assertThrows(TicketValidationBlockException.class,
                () -> userService.sendSignupTicket(requestDto));
    }

    @Test
    void send_ticket_time_limit_exception_is_propagated() throws Exception {
        SendSignupTicketRequestDto requestDto = new SendSignupTicketRequestDto();
        requestDto.setMobileNumber("09121111118");
        doThrow(new com.ecommerce.application.api.exception.SendTicketTimeLimitNotExceededException(
                "too fast"))
                .when(signupTicketService).sendTicket(any());

        assertThrows(com.ecommerce.application.api.exception.SendTicketTimeLimitNotExceededException.class,
                () -> userService.sendSignupTicket(requestDto));
    }
}
