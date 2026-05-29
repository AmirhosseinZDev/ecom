package com.telegram.ecommerce.application.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/29/26
 */
@Getter
@AllArgsConstructor
public class CheckUserRegistrationResponseDto {

    private boolean isRegistered;
}
