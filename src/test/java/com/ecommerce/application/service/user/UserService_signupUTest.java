package com.ecommerce.application.service.user;

import com.ecommerce.application.api.dto.user.SignupRequestDto;
import com.ecommerce.application.api.exception.InvalidSignupTokenException;
import com.ecommerce.application.api.exception.UserHasAlreadyExistException;
import com.ecommerce.application.config.properties.dto.LoginProperties;
import com.ecommerce.application.config.properties.dto.SignupProperties;
import com.ecommerce.application.service.jwt.JwtService;
import com.ecommerce.application.service.ticket.LoginTicketService;
import com.ecommerce.application.service.ticket.SignupTicketService;
import com.ecommerce.persistence.cache.SignupCacheService;
import com.ecommerce.persistence.cache.dto.SignupData;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserService_signupUTest {

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
        userService = new UserService(appUserRepository, passwordEncoder, authenticationManager,
                jwtService, signupTicketService, loginTicketService, signupCacheService,
                new SignupProperties(), new LoginProperties());
    }

    @Test
    void valid_signup_token_saves_user_and_deletes_token() throws Exception {
        SignupRequestDto requestDto = requestDto();
        when(signupCacheService.getSignupData("signup-token"))
                .thenReturn(new SignupData("09121111118"));
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("plain-password")).thenReturn("encoded-password");

        userService.signup(requestDto);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertEquals("John", captor.getValue().getFirstName());
        assertEquals("Doe", captor.getValue().getLastName());
        assertEquals("09121111118", captor.getValue().getUsername());
        assertEquals("encoded-password", captor.getValue().getPassword());
        assertTrue(captor.getValue().getIsEnabled());
        assertTrue(captor.getValue().getIsRegistered());
        verify(signupCacheService).deleteSignupData("signup-token");
    }

    @Test
    void missing_signup_token_throws_invalid_signup_token_exception() {
        SignupRequestDto requestDto = requestDto();
        when(signupCacheService.getSignupData("signup-token")).thenReturn(null);

        assertThrows(InvalidSignupTokenException.class, () -> userService.signup(requestDto));

        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void existing_registered_user_throws_user_already_exists_exception() {
        SignupRequestDto requestDto = requestDto();
        when(signupCacheService.getSignupData("signup-token"))
                .thenReturn(new SignupData("09121111118"));
        AppUser existingUser = new AppUser();
        existingUser.setIsRegistered(true);
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.of(existingUser));

        assertThrows(UserHasAlreadyExistException.class, () -> userService.signup(requestDto));

        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(signupCacheService, never()).deleteSignupData(org.mockito.ArgumentMatchers.anyString());
    }

    private SignupRequestDto requestDto() {
        SignupRequestDto requestDto = new SignupRequestDto();
        requestDto.setSignupToken("signup-token");
        requestDto.setFirstName("John");
        requestDto.setLastName("Doe");
        requestDto.setPassword("plain-password");
        return requestDto;
    }
}
