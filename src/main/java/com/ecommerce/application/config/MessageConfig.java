package com.ecommerce.application.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * @author reza gholamzad
 * @since 6/11/26
 */
@Configuration
public class MessageConfig {

    @Bean
    protected MessageSource messageSource() {
        final var messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasenames(
                "classpath:/messages"
        );
        messageSource.setDefaultEncoding("UTF-8");

        return messageSource;
    }

}
