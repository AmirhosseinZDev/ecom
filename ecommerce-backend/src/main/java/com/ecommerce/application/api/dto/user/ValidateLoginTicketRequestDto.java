package com.ecommerce.application.api.dto.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/29/26
 */
@Getter
@Setter
public class ValidateLoginTicketRequestDto {

    @NotEmpty
    @Pattern(regexp = "^09[0-9]{9}$", message = "Invalid mobile number format")
    private String mobileNumber;

    @NotEmpty
    private String ticket;
}
