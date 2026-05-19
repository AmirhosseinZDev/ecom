package com.telegram.ecommerce.application.api.dto.dashboard;

import com.tosan.validation.constraints.UUID;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@Getter
@Setter
public class SignupRequestDto {

    @NotEmpty
    @UUID
    private String signupToken;

    @NotEmpty
    private String password;

    @NotEmpty
    private String name;
}
