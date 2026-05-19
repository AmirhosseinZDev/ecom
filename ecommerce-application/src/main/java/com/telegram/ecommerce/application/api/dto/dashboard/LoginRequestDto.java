package com.telegram.ecommerce.application.api.dto.dashboard;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 3/24/25
 */
@Getter
@Setter
public class LoginRequestDto {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;
}
