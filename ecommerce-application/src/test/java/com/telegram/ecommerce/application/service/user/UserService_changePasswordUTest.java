package com.telegram.ecommerce.application.service.user;

import com.telegram.ecommerce.application.api.dto.user.ChangePasswordRequestDto;
import com.telegram.ecommerce.application.api.exception.InvalidPasswordException;
import com.telegram.ecommerce.application.api.exception.UserNotFoundException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserService_changePasswordUTest {

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
    void matching_passwords_encode_and_persist_new_password() throws Exception {
        AppUser appUser = new AppUser();
        appUser.setPassword("old-encoded");
        when(appUserRepository.findById(1L)).thenReturn(Optional.of(appUser));
        when(passwordEncoder.encode("new-pass")).thenReturn("new-encoded");

        userService.changePassword(requestDto("new-pass", "new-pass"), 1L);

        assertEquals("new-encoded", appUser.getPassword());
        verify(appUserRepository).save(appUser);
    }

    @Test
    void mismatched_new_and_confirm_passwords_throws_invalid_password_exception() {
        assertThrows(InvalidPasswordException.class,
                () -> userService.changePassword(requestDto("abc", "xyz"), 1L));

        verifyNoInteractions(appUserRepository);
    }

    @Test
    void user_not_found_throws_user_not_found_exception() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.changePassword(requestDto("new-pass", "new-pass"), 99L));

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void mismatched_passwords_check_happens_before_db_lookup() throws Exception {
        assertThrows(InvalidPasswordException.class,
                () -> userService.changePassword(requestDto("a", "b"), 1L));

        verifyNoInteractions(appUserRepository);
    }

    private ChangePasswordRequestDto requestDto(String newPassword, String confirmPassword) {
        ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto();
        requestDto.setNewPassword(newPassword);
        requestDto.setConfirmPassword(confirmPassword);
        return requestDto;
    }
}
