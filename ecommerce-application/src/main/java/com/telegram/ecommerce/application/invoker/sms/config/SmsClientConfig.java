package com.telegram.ecommerce.application.invoker.sms.config;

import com.telegram.ecommerce.application.invoker.interceptor.HttpLoggingInterceptor;
import com.telegram.ecommerce.application.invoker.interceptor.HttpLoggingInterceptorUtil;
import com.telegram.ecommerce.application.invoker.sms.client.SmsClient;
import com.telegram.ecommerce.application.invoker.sms.config.properties.SmsProperties;
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
public class SmsClientConfig {

    private static final String X_API_KEY_HEADER = "x-api-key";
    private static final String SERVICE_NAME = "SmsClient";
    private final HttpLoggingInterceptorUtil httpLoggingInterceptorUtil;

    @Bean
    public RestClient smsRestClient(SmsProperties smsProperties) {
        return RestClient.builder()
                .baseUrl(smsProperties.getBaseUrl())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(X_API_KEY_HEADER, smsProperties.getApiKey())
                .requestInterceptor(new HttpLoggingInterceptor(httpLoggingInterceptorUtil, SERVICE_NAME))
                .build();
    }

    @Bean
    public SmsClient smsVerificationClient(RestClient smsRestClient) {
        return HttpServiceProxyFactory
                .builderFor(RestClientAdapter.create(smsRestClient))
                .build()
                .createClient(SmsClient.class);
    }
}