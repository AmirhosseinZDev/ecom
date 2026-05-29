package com.telegram.ecommerce.application.service.user;

import com.telegram.ecommerce.application.api.dto.user.CheckUserRegistrationRequestDto;
import com.telegram.ecommerce.application.api.dto.user.CheckUserRegistrationResponseDto;
import com.telegram.ecommerce.application.config.properties.dto.LoginProperties;
import com.telegram.ecommerce.application.config.properties.dto.SignupProperties;
import com.telegram.ecommerce.application.service.jwt.JwtService;
import com.telegram.ecommerce.application.service.ticket.LoginTicketService;
import com.telegram.ecommerce.application.service.ticket.SignupTicketService;
import com.telegram.ecommerce.persistence.cache.SignupCacheService;
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserService_checkUserRegistrationUTest {

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
    void returns_true_when_user_exists_and_is_registered() {
        AppUser appUser = new AppUser();
        appUser.setIsRegistered(true);
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.of(appUser));

        CheckUserRegistrationResponseDto response = userService.checkUserRegistration(requestDto());

        assertTrue(response.isRegistered());
    }

    @Test
    void returns_false_when_user_exists_but_not_registered() {
        AppUser appUser = new AppUser();
        appUser.setIsRegistered(false);
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.of(appUser));

        CheckUserRegistrationResponseDto response = userService.checkUserRegistration(requestDto());

        assertFalse(response.isRegistered());
    }

    @Test
    void returns_false_when_user_does_not_exist() {
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.empty());

        CheckUserRegistrationResponseDto response = userService.checkUserRegistration(requestDto());

        assertFalse(response.isRegistered());
    }

    @Test
    void returns_false_when_is_registered_is_null() {
        AppUser appUser = new AppUser();
        appUser.setIsRegistered(null);
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.of(appUser));

        CheckUserRegistrationResponseDto response = userService.checkUserRegistration(requestDto());

        assertFalse(response.isRegistered());
    }

    private CheckUserRegistrationRequestDto requestDto() {
        CheckUserRegistrationRequestDto requestDto = new CheckUserRegistrationRequestDto();
        requestDto.setMobileNumber("09121111118");
        return requestDto;
    }
}
