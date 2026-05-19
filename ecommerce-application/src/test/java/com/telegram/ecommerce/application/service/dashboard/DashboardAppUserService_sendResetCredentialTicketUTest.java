package com.telegram.ecommerce.application.service.dashboard;

import com.telegram.ecommerce.application.api.dto.dashboard.SendResetCredentialTicketRequestDto;
import com.telegram.ecommerce.application.api.dto.dashboard.SendResetCredentialTicketResponseDto;
import com.telegram.ecommerce.application.api.exception.TicketValidationBlockException;
import com.telegram.ecommerce.application.api.exception.UserNotFoundException;
import com.telegram.ecommerce.application.config.properties.dto.SignupProperties;
import com.telegram.ecommerce.application.config.properties.dto.TicketProperties;
import com.telegram.ecommerce.application.invoker.shahkar.ShahkarService;
import com.telegram.ecommerce.application.service.jwt.JwtService;
import com.telegram.ecommerce.application.service.ticket.SignupTicketService;
import com.telegram.ecommerce.application.service.ticket.dto.TicketGenerateRequestDto;
import com.telegram.ecommerce.persistence.cache.SignupCacheService;
import com.telegram.ecommerce.persistence.entity.AppUser;
import com.telegram.ecommerce.persistence.repository.AppUserRepository;
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
class DashboardAppUserService_sendResetCredentialTicketUTest {

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
    private SignupCacheService signupCacheService;
    @Mock
    private ShahkarService shahkarService;

    private DashboardUserService dashboardUserService;

    @BeforeEach
    void setUp() {
        SignupProperties signupProperties = new SignupProperties();
        TicketProperties ticketProperties = new TicketProperties();
        ticketProperties.setTemplateId(123456);
        ticketProperties.setTimeToLive(Duration.ofMinutes(2));
        signupProperties.setTicket(ticketProperties);
        dashboardUserService = new DashboardUserService(appUserRepository, passwordEncoder, authenticationManager,
                jwtService, signupTicketService, signupCacheService, signupProperties, shahkarService);
    }

    @Test
    void existing_user_sends_ticket_to_user_mobile_and_returns_ttl_and_mobile_number() throws Exception {
        SendResetCredentialTicketRequestDto requestDto = requestDto();
        AppUser appUser = new AppUser();
        appUser.setMobile("09121111118");
        when(appUserRepository.findByUsername("0021111112")).thenReturn(Optional.of(appUser));

        SendResetCredentialTicketResponseDto responseDto = dashboardUserService.sendResetCredentialTicket(requestDto);

        assertEquals(120L, responseDto.getTicketTTLInSecond());
        assertEquals("09121111118", responseDto.getMobileNumber());
        ArgumentCaptor<TicketGenerateRequestDto> captor = ArgumentCaptor.forClass(TicketGenerateRequestDto.class);
        verify(signupTicketService).sendTicket(captor.capture());
        assertEquals("0021111112+09121111118", captor.getValue().getCacheKey());
        assertEquals("lastSentTicketDateForSignup_09121111118", captor.getValue().getLastSentCacheKey());
        assertEquals("09121111118", captor.getValue().getMobileNumber());
        assertEquals(123456, captor.getValue().getSmsTemplateId());
        assertEquals(120, captor.getValue().getTicketTimeToLive());
    }

    @Test
    void missing_user_throws_user_not_found_without_sending_ticket() throws TicketValidationBlockException {
        SendResetCredentialTicketRequestDto requestDto = requestDto();
        when(appUserRepository.findByUsername("0021111112")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> dashboardUserService.sendResetCredentialTicket(requestDto));

        verify(signupTicketService, never()).sendTicket(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void ticket_block_exception_is_propagated() throws Exception {
        SendResetCredentialTicketRequestDto requestDto = requestDto();
        AppUser appUser = new AppUser();
        appUser.setMobile("09121111118");
        when(appUserRepository.findByUsername("0021111112")).thenReturn(Optional.of(appUser));
        doThrow(new TicketValidationBlockException("This mobile number has been blocked.")).when(signupTicketService)
                .sendTicket(org.mockito.ArgumentMatchers.any());

        assertThrows(TicketValidationBlockException.class,
                () -> dashboardUserService.sendResetCredentialTicket(requestDto));
    }

    private SendResetCredentialTicketRequestDto requestDto() {
        SendResetCredentialTicketRequestDto requestDto = new SendResetCredentialTicketRequestDto();
        requestDto.setNationalCode("0021111112");
        return requestDto;
    }
}
