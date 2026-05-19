package com.telegram.ecommerce.application.service.ticket.dto;

import lombok.Data;

import java.util.Map;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@Data
public class TicketGenerateRequestDto {

    private String cacheKey;
    private String lastSentCacheKey;
    private String mobileNumber;
    private Integer smsTemplateId;
    private int ticketTimeToLive;
    private int ticketLength;
    private Map<String, String> params;
}
