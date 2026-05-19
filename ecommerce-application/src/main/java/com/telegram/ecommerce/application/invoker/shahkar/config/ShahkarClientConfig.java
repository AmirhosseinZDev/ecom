package com.telegram.ecommerce.application.invoker.shahkar.config;

import com.telegram.ecommerce.application.invoker.interceptor.HttpLoggingInterceptor;
import com.telegram.ecommerce.application.invoker.interceptor.HttpLoggingInterceptorUtil;
import com.telegram.ecommerce.application.invoker.shahkar.client.ShahkarClient;
import com.telegram.ecommerce.application.invoker.shahkar.config.properties.ShahkarProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@Configuration
@RequiredArgsConstructor
public class ShahkarClientConfig {

    private static final String SERVICE_NAME = "ShahkarClient";
    private final HttpLoggingInterceptorUtil httpLoggingInterceptorUtil;

    @Bean
    public RestClient shahkarRestClient(ShahkarProperties shahkarProperties) {
        return RestClient.builder()
                .baseUrl(shahkarProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + shahkarProperties.getToken())
                .requestInterceptor(new HttpLoggingInterceptor(httpLoggingInterceptorUtil, SERVICE_NAME))
                .build();
    }

    @Bean
    public ShahkarClient shahkarClient(RestClient shahkarRestClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(shahkarRestClient))
                .build()
                .createClient(ShahkarClient.class);
    }
}
