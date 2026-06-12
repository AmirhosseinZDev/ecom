package com.ecommerce.application.service.user;

import com.ecommerce.application.api.dto.user.ChangePasswordRequestDto;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.application.config.properties.LoginProperties;
import com.ecommerce.application.config.properties.SignupProperties;
import com.ecommerce.application.service.jwt.JwtService;
import com.ecommerce.application.service.ticket.LoginTicketService;
import com.ecommerce.application.service.ticket.SignupTicketService;
import com.ecommerce.persistence.cache.SignupCacheService;
import com.ecommerce.persistence.entity.AppUser;
import com.ecommerce.persistence.repository.AppUserRepository;
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
        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> userService.changePassword(requestDto("abc", "xyz"), 1L));

        assertEquals(ECOMErrorType.INVALID_PASSWORD, exception.getEcomErrorType());

        verifyNoInteractions(appUserRepository);
    }

    @Test
    void user_not_found_throws_user_not_found_exception() {
        when(appUserRepository.findById(99L)).thenReturn(Optional.empty());

        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> userService.changePassword(requestDto("new-pass", "new-pass"), 99L));

        assertEquals(ECOMErrorType.USER_NOT_FOUND, exception.getEcomErrorType());

        verify(appUserRepository, never()).save(any());
    }

    @Test
    void mismatched_passwords_check_happens_before_db_lookup() throws Exception {
        EcommerceException exception = assertThrows(EcommerceException.class,
                () -> userService.changePassword(requestDto("a", "b"), 1L));

        assertEquals(ECOMErrorType.INVALID_PASSWORD, exception.getEcomErrorType());

        verifyNoInteractions(appUserRepository);
    }

    private ChangePasswordRequestDto requestDto(String newPassword, String confirmPassword) {
        ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto();
        requestDto.setNewPassword(newPassword);
        requestDto.setConfirmPassword(confirmPassword);
        return requestDto;
    }
}
