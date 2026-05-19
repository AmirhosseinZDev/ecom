package com.telegram.ecommerce.application;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * @author AmirHossein ZamanZade
 * @since 12/25/25
 */
@SpringBootApplication(scanBasePackages = "com.telegram.ecommerce")
@EnableJpaRepositories("com.telegram.ecommerce.persistence.repository")
@EntityScan("com.telegram.ecommerce.persistence.entity")
@EnableConfigurationProperties
public class EcommerceApplication {

    public static void main(String[] args) {
        if (new FileSystemResource("config/logback.xml").getFile().exists()) {
            System.setProperty("logging.config", "file:config/logback.xml");
        } else {
            System.setProperty("logging.config", "classpath:config/logback.xml");
        }
        new SpringApplicationBuilder(EcommerceApplication.class).run(args);
    }
}

