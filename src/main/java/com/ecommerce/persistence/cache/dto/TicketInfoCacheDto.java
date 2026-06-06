package com.ecommerce.persistence.cache.dto;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author AmirHossein ZamanZade
 * @since 11/15/2023
 */
@Data
public class TicketInfoCacheDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 145934483237970837L;
    private final String ticket;
    private int failureCount;

    public TicketInfoCacheDto(String ticket) {
        this.ticket = ticket;
    }

    public int incrementAndGetFailureCount() {
        return ++failureCount;
    }
}
