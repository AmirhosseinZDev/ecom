package com.telegram.ecommerce.application.service.dashboard;

import com.telegram.ecommerce.application.api.dto.dashboard.ResetCredentialRequestDto;
import com.telegram.ecommerce.application.api.exception.InvalidSignupTokenException;
import com.telegram.ecommerce.application.api.exception.UserNotFoundException;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardAppUserService_resetCredentialUTest {

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
        dashboardUserService = new DashboardUserService(appUserRepository, passwordEncoder, authenticationManager,
                jwtService, signupTicketService, signupCacheService, new SignupProperties(), shahkarService);
    }

    @Test
    void valid_signup_token_updates_password_and_deletes_token() throws Exception {
        ResetCredentialRequestDto requestDto = requestDto();
        AppUser appUser = new AppUser();
        appUser.setUsername("0021111112");
        appUser.setPassword("old-password");
        when(signupCacheService.getSignupData("signup-token"))
                .thenReturn(new SignupData("0021111112", "09121111118"));
        when(appUserRepository.findByUsername("0021111112")).thenReturn(Optional.of(appUser));
        when(passwordEncoder.encode("new-password")).thenReturn("encoded-new-password");

        dashboardUserService.resetCredential(requestDto);

        assertEquals("encoded-new-password", appUser.getPassword());
        verify(appUserRepository).save(appUser);
        verify(signupCacheService).deleteSignupData("signup-token");
        verifyNoInteractions(shahkarService, signupTicketService);
    }

    @Test
    void missing_signup_token_throws_invalid_signup_token_exception() {
        ResetCredentialRequestDto requestDto = requestDto();
        when(signupCacheService.getSignupData("signup-token")).thenReturn(null);

        assertThrows(InvalidSignupTokenException.class, () -> dashboardUserService.resetCredential(requestDto));

        verify(appUserRepository, never()).findByUsername(org.mockito.ArgumentMatchers.anyString());
        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(signupCacheService, never()).deleteSignupData(org.mockito.ArgumentMatchers.anyString());
    }

    @Test
    void missing_user_throws_user_not_found_without_deleting_token() {
        ResetCredentialRequestDto requestDto = requestDto();
        when(signupCacheService.getSignupData("signup-token"))
                .thenReturn(new SignupData("0021111112", "09121111118"));
        when(appUserRepository.findByUsername("0021111112")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> dashboardUserService.resetCredential(requestDto));

        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(signupCacheService, never()).deleteSignupData(org.mockito.ArgumentMatchers.anyString());
    }

    private ResetCredentialRequestDto requestDto() {
        ResetCredentialRequestDto requestDto = new ResetCredentialRequestDto();
        requestDto.setSignupToken("signup-token");
        requestDto.setNewPassword("new-password");
        return requestDto;
    }
}
