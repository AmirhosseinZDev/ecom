package com.telegram.ecommerce.application.invoker.sms.config.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@Getter
@Setter
public class SmsProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String apiKey;
}
