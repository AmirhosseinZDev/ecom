package com.ecommerce.application.api.dto.user;

import com.tosan.validation.constraints.MobileNumber;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/29/26
 */
@Getter
@Setter
public class CheckUserRegistrationRequestDto {

    @NotEmpty
    @MobileNumber
    private String mobileNumber;
}
