package com.ecommerce.application.config.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;

/**
 * @author AmirHossein ZamanZade
 * @since 11/15/2023
 */
@Getter
@Setter
public class TicketProperties {

    @NotNull
    private Duration timeToLive;

    @NotEmpty
    private Integer templateId;

    @NotNull
    private Integer maxFailureCount;

    @NotNull
    private Duration blockDuration;

    private Integer length;
}
