package com.telegram.ecommerce.application.api.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/10/26
 */
@Getter
@AllArgsConstructor
public class SendResetCredentialTicketResponseDto {

    private Long ticketTTLInSecond;
    private String mobileNumber;
}
