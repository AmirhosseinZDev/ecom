package com.telegram.ecommerce.application.service.user;

import com.telegram.ecommerce.application.api.dto.user.LoginResponseDto;
import com.telegram.ecommerce.application.api.dto.user.ValidateLoginTicketRequestDto;
import com.telegram.ecommerce.application.api.dto.user.enumeration.Role;
import com.telegram.ecommerce.application.api.exception.InvalidTicketException;
import com.telegram.ecommerce.application.api.exception.TicketValidationBlockException;
import com.telegram.ecommerce.application.api.exception.UserNotFoundException;
import com.telegram.ecommerce.application.config.properties.dto.LoginProperties;
import com.telegram.ecommerce.application.config.properties.dto.SignupProperties;
import com.telegram.ecommerce.application.service.jwt.JwtService;
import com.telegram.ecommerce.application.service.ticket.LoginTicketService;
import com.telegram.ecommerce.application.service.ticket.SignupTicketService;
import com.telegram.ecommerce.persistence.cache.SignupCacheService;
import com.telegram.ecommerce.persistence.entity.AppUser;
import com.telegram.ecommerce.persistence.entity.enumeration.UserRole;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserService_validateLoginTicketUTest {

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
    void valid_otp_for_registered_user_returns_jwt_and_role() throws Exception {
        AppUser appUser = new AppUser();
        appUser.setId(1L);
        appUser.setUsername("09121111118");
        appUser.setPassword("encoded");
        appUser.setRole(UserRole.ROLE_APP_USER);
        appUser.setIsEnabled(true);
        appUser.setIsRegistered(true);
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.of(appUser));
        when(jwtService.generateToken(any())).thenReturn("jwt-token");

        LoginResponseDto responseDto = userService.validateLoginTicket(requestDto("09121111118", "123456"));

        verify(loginTicketService).validateTicket("09121111118", "123456", "09121111118");
        assertEquals("jwt-token", responseDto.getToken());
        assertEquals(Role.ROLE_APP_USER, responseDto.getRole());
    }

    @Test
    void invalid_ticket_exception_is_propagated_before_user_lookup() throws Exception {
        doThrow(new InvalidTicketException("invalid"))
                .when(loginTicketService).validateTicket(any(), any(), any());

        assertThrows(InvalidTicketException.class,
                () -> userService.validateLoginTicket(requestDto("09121111118", "000000")));

        verifyNoInteractions(appUserRepository);
    }

    @Test
    void block_exception_is_propagated_before_user_lookup() throws Exception {
        doThrow(new TicketValidationBlockException("blocked"))
                .when(loginTicketService).validateTicket(any(), any(), any());

        assertThrows(TicketValidationBlockException.class,
                () -> userService.validateLoginTicket(requestDto("09121111118", "000000")));

        verifyNoInteractions(appUserRepository);
    }

    @Test
    void throws_user_not_found_when_mobile_not_in_db_after_valid_otp() throws Exception {
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.validateLoginTicket(requestDto("09121111118", "123456")));
    }

    @Test
    void throws_user_not_found_when_user_is_not_registered_after_valid_otp() throws Exception {
        AppUser appUser = new AppUser();
        appUser.setIsRegistered(false);
        when(appUserRepository.findByMobile("09121111118")).thenReturn(Optional.of(appUser));

        assertThrows(UserNotFoundException.class,
                () -> userService.validateLoginTicket(requestDto("09121111118", "123456")));
    }

    private ValidateLoginTicketRequestDto requestDto(String mobile, String ticket) {
        ValidateLoginTicketRequestDto requestDto = new ValidateLoginTicketRequestDto();
        requestDto.setMobileNumber(mobile);
        requestDto.setTicket(ticket);
        return requestDto;
    }
}
