package com.ecommerce.application.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

/**
 * @author E.habibi
 * @since 5/23/2023
 */
@Getter
@Setter
public class SignupProperties {

    @NestedConfigurationProperty
    private TicketProperties ticket;
    private Duration tokenTtl;
}
