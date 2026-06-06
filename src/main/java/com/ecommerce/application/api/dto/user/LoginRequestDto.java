package com.ecommerce.application.api.dto.user;

import com.tosan.validation.constraints.MobileNumber;
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
    @MobileNumber
    private String mobileNumber;

    @NotEmpty
    private String password;
}
