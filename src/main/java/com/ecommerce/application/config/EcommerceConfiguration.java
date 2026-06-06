package com.ecommerce.application.config;

import com.ecommerce.application.util.ResourceUtil;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.tosan.validation.Validation;
import com.tosan.validation.core.ValidatorBuilder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author Amirhossein Zamanzade
 * @since 8/18/25
 */
@Configuration
@EnableWebMvc
public class EcommerceConfiguration implements WebMvcConfigurer {

    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssZ";


    @Bean("devPropertySourcesPlaceholderConfigurer")
    @Profile("dev")
    public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(ResourceUtil resourceUtil) {
        return generatePlaceHolderConfigurer(resourceUtil, "/config/application-dev.properties",
                "config/application-dev.properties");
    }

    @Bean("testPropertySourcesPlaceholderConfigurer")
    @Profile("test")
    public PropertySourcesPlaceholderConfigurer testPropertySourcesPlaceholderConfigurer(ResourceUtil resourceUtil) {
        return generatePlaceHolderConfigurer(resourceUtil, "/config/application-test.properties",
                "config/application-test.properties");
    }

    @Bean("propertySourcesPlaceholderConfigurer")
    @ConditionalOnMissingBean(PropertySourcesPlaceholderConfigurer.class)
    public PropertySourcesPlaceholderConfigurer productionPropertySourcesPlaceholderConfigurer(
            ResourceUtil resourceUtil) {
        return generatePlaceHolderConfigurer(resourceUtil, null, null);
    }

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return JsonMapper.builder()
                .defaultDateFormat(new SimpleDateFormat(DATE_TIME_PATTERN))
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
                .build();
    }

    @Bean
    @Primary
    public Validation yekanValidation() {
        Validation validation = new Validation(new ValidatorBuilder(), new HashMap<>());
        validation.setIgnoredParameters(new ArrayList<>());
        validation.setSemiIgnoredParameters(getSemiIgnoredParameters());
        return validation;
    }

    private EcommercePlaceHolderConfigurer generatePlaceHolderConfigurer(ResourceUtil resourceUtil, String classpath,
            String fileSystemPath) {
        EcommercePlaceHolderConfigurer placeholderConfigurer = new EcommercePlaceHolderConfigurer(resourceUtil);
        placeholderConfigurer.setFileEncoding("UTF-8");
        placeholderConfigurer.setLocations(getResources(classpath, fileSystemPath));
        placeholderConfigurer.setIgnoreResourceNotFound(true);
        placeholderConfigurer.setLocalOverride(true);
        return placeholderConfigurer;
    }

    private Resource[] getResources(String classpath, String fileSystemPath) {
        if (classpath != null && fileSystemPath != null) {
            return new Resource[]{
                    new ClassPathResource("/config/application.properties"),
                    new ClassPathResource(classpath),
                    new FileSystemResource(fileSystemPath),
                    new ClassPathResource("/config/application.yml")
            };
        } else {
            return new Resource[]{
                    new ClassPathResource("/config/application.properties"),
                    new FileSystemResource("config/application.properties"),
                    new ClassPathResource("/config/application.yml")
            };
        }
    }

    private List<String> getSemiIgnoredParameters() {
        return List.of();
    }
}
