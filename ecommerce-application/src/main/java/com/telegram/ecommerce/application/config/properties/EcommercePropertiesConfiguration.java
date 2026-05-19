package com.telegram.ecommerce.application.config.properties;

import com.telegram.ecommerce.application.config.properties.dto.SignupProperties;
import com.telegram.ecommerce.application.invoker.shahkar.config.properties.ShahkarProperties;
import com.telegram.ecommerce.application.invoker.sms.config.properties.SmsProperties;
import jakarta.validation.Valid;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@Configuration
public class EcommercePropertiesConfiguration {

    @Bean
    @Valid
    @ConfigurationProperties(prefix = "signup")
    public SignupProperties signupProperties() {
        return new SignupProperties();
    }

    @Bean
    @Valid
    @ConfigurationProperties(prefix = "sms")
    public SmsProperties smsProperties() {
        return new SmsProperties();
    }

    @Bean
    @Valid
    @ConfigurationProperties(prefix = "shahkar")
    public ShahkarProperties shahkarProperties() {
        return new ShahkarProperties();
    }
}
