package com.ecommerce.application.service.user;

import com.ecommerce.application.api.dto.user.SendLoginTicketRequestDto;
import com.ecommerce.application.api.dto.user.SendSignupTicketResponseDto;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.application.config.properties.LoginProperties;
import com.ecommerce.application.config.properties.SignupProperties;
import com.ecommerce.application.config.properties.TicketProperties;
import com.ecommerce.application.service.jwt.JwtService;
import com.ecommerce.application.service.ticket.LoginTicketService;
import com.ecommerce.application.service.ticket.SignupTicketService;
import com.ecommerce.application.service.ticket.dto.TicketGenerateRequestDto;
import com.ecommerce.persistence.cache.SignupCacheService;
import com.ecommerce.persistence.entity.AppUser;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserService_sendLoginTicketUTest {

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
        LoginProperties loginProperties = new LoginProperties();
        TicketProperties ticketProperties = new TicketProperties();
        ticketProperties.setTemplateId(200001);
        ticketProperties.setTimeToLive(Duration.ofMinutes(2));
        loginProperties.setTicket(ticketProperties);
        userService = new UserService(appUserRepository, passwordEncoder, authenticationManager,
                jwtService, signupTicketService, loginTicketService, signupCacheService,
                new SignupProperties(), loginProperties);
    }

    @Test
    void sends_ticket_to_registered_user_and_returns_ttl() throws Exception {
        AppUser appUser = new AppUser();
        appUser.setIsRegistered(true);
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.of(appUser));

        SendSignupTicketResponseDto responseDto = userService.sendLoginTicket(requestDto());

        assertEquals(120L, responseDto.getTicketTTLInSecond());
        ArgumentCaptor<TicketGenerateRequestDto> captor = ArgumentCaptor.forClass(TicketGenerateRequestDto.class);
        verify(loginTicketService).sendTicket(captor.capture());
        TicketGenerateRequestDto sent = captor.getValue();
        assertEquals("09121111118", sent.getMobileNumber());
        assertEquals("09121111118", sent.getCacheKey());
        assertEquals("09121111118", sent.getLastSentCacheKey());
        assertEquals(200001, sent.getSmsTemplateId());
        assertEquals(120, sent.getTicketTimeToLive());
    }

    @Test
    void throws_user_not_found_when_mobile_not_in_db() {
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.empty());

        EcommerceException exception = assertThrows(EcommerceException.class, () -> userService.sendLoginTicket(requestDto()));

        assertEquals(ECOMErrorType.USER_NOT_FOUND, exception.getEcomErrorType());

        verifyNoInteractions(loginTicketService);
    }

    @Test
    void throws_user_not_found_when_user_exists_but_not_registered() {
        AppUser appUser = new AppUser();
        appUser.setIsRegistered(false);
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.of(appUser));

        EcommerceException exception = assertThrows(EcommerceException.class, () -> userService.sendLoginTicket(requestDto()));

        assertEquals(ECOMErrorType.USER_NOT_FOUND, exception.getEcomErrorType());

        verifyNoInteractions(loginTicketService);
    }

    @Test
    void block_exception_is_propagated() throws Exception {
        AppUser appUser = new AppUser();
        appUser.setIsRegistered(true);
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.of(appUser));
        doThrow(new EcommerceException(ECOMErrorType.TICKET_BLOCKED))
                .when(loginTicketService).sendTicket(any());

        EcommerceException exception = assertThrows(EcommerceException.class, () -> userService.sendLoginTicket(requestDto()));

        assertEquals(ECOMErrorType.TICKET_BLOCKED, exception.getEcomErrorType());
    }

    private SendLoginTicketRequestDto requestDto() {
        SendLoginTicketRequestDto requestDto = new SendLoginTicketRequestDto();
        requestDto.setMobileNumber("09121111118");
        return requestDto;
    }
}
