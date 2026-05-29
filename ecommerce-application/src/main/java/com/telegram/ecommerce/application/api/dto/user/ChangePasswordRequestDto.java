package com.telegram.ecommerce.application.api.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/29/26
 */
@Getter
@Setter
public class ChangePasswordRequestDto {

    @NotEmpty
    private String newPassword;

    @NotEmpty
    private String confirmPassword;
}
