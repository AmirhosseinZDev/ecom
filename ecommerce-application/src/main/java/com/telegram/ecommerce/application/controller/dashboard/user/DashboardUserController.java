package com.telegram.ecommerce.application.controller.dashboard.user;

import com.telegram.ecommerce.application.api.dto.dashboard.*;
import com.telegram.ecommerce.application.api.exception.*;
import com.telegram.ecommerce.application.controller.dashboard.DashboardController;
import com.telegram.ecommerce.application.service.dashboard.DashboardUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@RestController
@RequestMapping(DashboardController.BASE_PATH + "/user")
@RequiredArgsConstructor
public class DashboardUserController {

    private final DashboardUserService dashboardUserService;

    @PostMapping(value = "/signup-ticket", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SendSignupTicketResponseDto sendSignupTicket(@RequestBody SendSignupTicketRequestDto requestDto)
            throws TicketValidationBlockException {
        return dashboardUserService.sendSignupTicket(requestDto);
    }

    @PostMapping(value = "/credential-ticket/validation", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SignupTicketValidationResponseDto validateCredentialTicket(
            @RequestBody SignupTicketValidationRequestDto requestDto)
            throws TicketValidationBlockException, InvalidTicketException {
        return dashboardUserService.validateCredentialTicket(requestDto);
    }

    @PostMapping(value = "/signup", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void signup(@RequestBody SignupRequestDto requestDto)
            throws UserHasAlreadyExistException, InvalidSignupTokenException,
            MismatchNationalCodeWithMobileNumberException, InvalidNationalCodeException {
        dashboardUserService.signup(requestDto);
    }

    @PostMapping(value = "/reset-credential-ticket", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public SendResetCredentialTicketResponseDto sendResetCredentialTicket(
            @RequestBody SendResetCredentialTicketRequestDto requestDto)
            throws TicketValidationBlockException, UserNotFoundException {
        return dashboardUserService.sendResetCredentialTicket(requestDto);
    }

    @PostMapping(value = "/reset-credential", consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public void resetCredential(@RequestBody ResetCredentialRequestDto requestDto)
            throws InvalidSignupTokenException, UserNotFoundException {
        dashboardUserService.resetCredential(requestDto);
    }

    @PostMapping(value = "/login", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public LoginResponseDto login(@RequestBody LoginRequestDto requestDto) {
        return dashboardUserService.login(requestDto);
    }
}
