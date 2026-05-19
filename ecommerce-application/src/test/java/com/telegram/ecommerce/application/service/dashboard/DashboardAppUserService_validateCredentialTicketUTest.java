package com.telegram.ecommerce.application.service.dashboard;

import com.telegram.ecommerce.application.api.dto.dashboard.SignupTicketValidationRequestDto;
import com.telegram.ecommerce.application.api.dto.dashboard.SignupTicketValidationResponseDto;
import com.telegram.ecommerce.application.config.properties.dto.SignupProperties;
import com.telegram.ecommerce.application.invoker.shahkar.ShahkarService;
import com.telegram.ecommerce.application.service.jwt.JwtService;
import com.telegram.ecommerce.application.service.ticket.SignupTicketService;
import com.telegram.ecommerce.persistence.cache.SignupCacheService;
import com.telegram.ecommerce.persistence.cache.dto.SignupData;
import com.telegram.ecommerce.persistence.entity.AppUser;
import com.telegram.ecommerce.persistence.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardAppUserService_validateCredentialTicketUTest {

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
        signupProperties.setTokenTtl(Duration.ofMinutes(2));
        dashboardUserService = new DashboardUserService(appUserRepository, passwordEncoder, authenticationManager,
                jwtService, signupTicketService, signupCacheService, signupProperties, shahkarService);
    }

    @Test
    void valid_ticket_and_new_user_creates_signup_token() throws Exception {
        SignupTicketValidationRequestDto requestDto = requestDto();
        when(appUserRepository.findByUsername("0021111112")).thenReturn(Optional.empty());

        SignupTicketValidationResponseDto responseDto = dashboardUserService.validateCredentialTicket(requestDto);

        assertNotNull(responseDto.getSignupToken());
        assertTrue(responseDto.isNewUser());
        verify(signupTicketService).validateTicket("0021111112+09121111118", "1234", "09121111118");
        verify(shahkarService, never()).match(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
        verify(signupCacheService).addSignupData(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(SignupData.class), org.mockito.ArgumentMatchers.eq(120L));
    }

    @Test
    void existing_user_creates_token_with_is_new_user_false_and_skips_shahkar() throws Exception {
        SignupTicketValidationRequestDto requestDto = requestDto();
        when(appUserRepository.findByUsername("0021111112")).thenReturn(Optional.of(new AppUser()));

        SignupTicketValidationResponseDto responseDto = dashboardUserService.validateCredentialTicket(requestDto);

        assertNotNull(responseDto.getSignupToken());
        assertFalse(responseDto.isNewUser());
        verify(shahkarService, never()).match(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString());
        verify(signupCacheService).addSignupData(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(SignupData.class), org.mockito.ArgumentMatchers.eq(120L));
    }

    private SignupTicketValidationRequestDto requestDto() {
        SignupTicketValidationRequestDto requestDto = new SignupTicketValidationRequestDto();
        requestDto.setNationalCode("0021111112");
        requestDto.setMobileNumber("09121111118");
        requestDto.setTicket("1234");
        return requestDto;
    }
}
