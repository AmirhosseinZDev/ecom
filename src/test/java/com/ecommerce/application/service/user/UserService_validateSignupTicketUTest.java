package com.ecommerce.application.service.user;

import com.ecommerce.application.api.dto.user.SignupTicketValidationRequestDto;
import com.ecommerce.application.api.dto.user.SignupTicketValidationResponseDto;
import com.ecommerce.application.config.properties.LoginProperties;
import com.ecommerce.application.config.properties.SignupProperties;
import com.ecommerce.application.service.jwt.JwtService;
import com.ecommerce.application.service.ticket.LoginTicketService;
import com.ecommerce.application.service.ticket.SignupTicketService;
import com.ecommerce.persistence.cache.SignupCacheService;
import com.ecommerce.persistence.cache.dto.SignupData;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserService_validateSignupTicketUTest {

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
        SignupProperties signupProperties = new SignupProperties();
        signupProperties.setTokenTtl(Duration.ofMinutes(2));
        userService = new UserService(appUserRepository, passwordEncoder, authenticationManager,
                jwtService, signupTicketService, loginTicketService, signupCacheService,
                signupProperties, new LoginProperties());
    }

    @Test
    void valid_ticket_creates_signup_token() throws Exception {
        SignupTicketValidationRequestDto requestDto = requestDto();

        SignupTicketValidationResponseDto responseDto = userService.validateSignupTicket(requestDto);

        assertNotNull(responseDto.getSignupToken());
        verify(signupTicketService).validateTicket("09121111118", "1234", "09121111118");
        ArgumentCaptor<SignupData> signupDataCaptor = ArgumentCaptor.forClass(SignupData.class);
        verify(signupCacheService).addSignupData(org.mockito.ArgumentMatchers.anyString(),
                signupDataCaptor.capture(), eq(120L));
        org.junit.jupiter.api.Assertions.assertEquals("09121111118", signupDataCaptor.getValue().getMobile());
    }

    private SignupTicketValidationRequestDto requestDto() {
        SignupTicketValidationRequestDto requestDto = new SignupTicketValidationRequestDto();
        requestDto.setMobileNumber("09121111118");
        requestDto.setTicket("1234");
        return requestDto;
    }
}
