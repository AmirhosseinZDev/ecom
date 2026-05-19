package com.telegram.ecommerce.application.api.dto.dashboard;

import com.tosan.validation.constraints.UUID;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/10/26
 */
@Getter
@Setter
public class ResetCredentialRequestDto {

    @NotEmpty
    @UUID
    private String signupToken;

    @NotEmpty
    private String newPassword;
}
