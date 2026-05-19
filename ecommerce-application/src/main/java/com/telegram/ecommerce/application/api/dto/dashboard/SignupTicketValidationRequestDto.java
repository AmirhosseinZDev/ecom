package com.telegram.ecommerce.application.api.dto.dashboard;

import com.tosan.validation.constraints.MobileNumber;
import com.tosan.validation.constraints.NationalCode;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/4/26
 */
@Getter
@Setter
public class SignupTicketValidationRequestDto {

    @NotEmpty
    private String ticket;

    @NotEmpty
    @NationalCode
    private String nationalCode;

    @NotEmpty
    @MobileNumber
    private String mobileNumber;
}
