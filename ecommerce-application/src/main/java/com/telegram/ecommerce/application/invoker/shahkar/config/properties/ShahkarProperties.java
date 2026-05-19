package com.telegram.ecommerce.application.invoker.shahkar.config.properties;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@Getter
@Setter
public class ShahkarProperties {

    @NotEmpty
    private String baseUrl;

    @NotEmpty
    private String token;
}
