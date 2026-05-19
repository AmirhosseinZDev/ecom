package com.telegram.ecommerce.application.service.dashboard;

import com.telegram.ecommerce.application.api.dto.dashboard.SignupRequestDto;
import com.telegram.ecommerce.application.api.exception.InvalidSignupTokenException;
import com.telegram.ecommerce.application.api.exception.UserHasAlreadyExistException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DashboardAppUserService_signupUTest {

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
    void valid_signup_token_saves_user_and_deletes_token() throws Exception {
        SignupRequestDto requestDto = requestDto();
        when(signupCacheService.getSignupData("signup-token"))
                .thenReturn(new SignupData("0021111112", "09121111118"));
        when(appUserRepository.findByUsername("0021111112")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");
        doNothing().when(shahkarService).match("0021111112", "09121111118");

        dashboardUserService.signup(requestDto);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertEquals("Test User", captor.getValue().getName());
        assertEquals("0021111112", captor.getValue().getUsername());
        assertEquals("09121111118", captor.getValue().getMobile());
        assertEquals("encoded-password", captor.getValue().getPassword());
        assertTrue(captor.getValue().getIsEnabled());
        verify(shahkarService).match("0021111112", "09121111118");
        verify(signupCacheService).deleteSignupData("signup-token");
    }

    @Test
    void missing_signup_token_throws_invalid_signup_token_exception() {
        SignupRequestDto requestDto = requestDto();
        when(signupCacheService.getSignupData("signup-token")).thenReturn(null);

        assertThrows(InvalidSignupTokenException.class, () -> dashboardUserService.signup(requestDto));

        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void existing_user_throws_user_already_exists_exception() {
        SignupRequestDto requestDto = requestDto();
        when(signupCacheService.getSignupData("signup-token"))
                .thenReturn(new SignupData("0021111112", "09121111118"));
        when(appUserRepository.findByUsername("0021111112")).thenReturn(Optional.of(new AppUser()));

        assertThrows(UserHasAlreadyExistException.class, () -> dashboardUserService.signup(requestDto));

        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(signupCacheService, never()).deleteSignupData(org.mockito.ArgumentMatchers.anyString());
    }

    private SignupRequestDto requestDto() {
        SignupRequestDto requestDto = new SignupRequestDto();
        requestDto.setSignupToken("signup-token");
        requestDto.setName("Test User");
        requestDto.setPassword("plain-password");
        return requestDto;
    }
}
