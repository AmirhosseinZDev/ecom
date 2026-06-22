package com.ecommerce.application.api.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/1/26
 */
@Getter
@AllArgsConstructor
public class SendSignupTicketResponseDto {
    private Long ticketTTLInSecond;
}
