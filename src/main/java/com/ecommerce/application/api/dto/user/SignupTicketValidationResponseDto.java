package com.ecommerce.application.api.dto.user;

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
}
