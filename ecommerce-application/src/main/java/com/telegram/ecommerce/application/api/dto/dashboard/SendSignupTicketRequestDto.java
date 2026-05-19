package com.telegram.ecommerce.application.api.dto.dashboard;

import com.tosan.validation.constraints.MobileNumber;
import com.tosan.validation.constraints.NationalCode;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/1/26
 */
@Getter
@Setter
public class SendSignupTicketRequestDto {

    @NotEmpty
    @NationalCode
    private String nationalCode;

    @NotEmpty
    @MobileNumber
    private String mobileNumber;
}
