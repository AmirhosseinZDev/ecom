package com.ecommerce.application.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;

/**
 * @author AmirHossein ZamanZade
 * @since 5/29/26
 */
@Getter
@Setter
public class LoginProperties {

    @NestedConfigurationProperty
    private TicketProperties ticket;
    private Duration tokenTtl;
}
