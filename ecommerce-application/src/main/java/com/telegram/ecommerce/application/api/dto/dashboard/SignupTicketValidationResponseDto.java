package com.telegram.ecommerce.application.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/4/26
 */
@Getter
@AllArgsConstructor
public class SignupTicketValidationResponseDto {

    private String signupToken;
    private boolean isNewUser;
}
