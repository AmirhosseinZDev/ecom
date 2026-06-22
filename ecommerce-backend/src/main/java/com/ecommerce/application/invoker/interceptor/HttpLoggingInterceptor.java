package com.ecommerce.application.invoker.interceptor;

import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.application.invoker.interceptor.wrraper.HttpResponseWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@Slf4j
@RequiredArgsConstructor
public class HttpLoggingInterceptor implements ClientHttpRequestInterceptor {

    private final HttpLoggingInterceptorUtil httpLoggingInterceptorUtil;
    private final String webServiceName;

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] requestBody, ClientHttpRequestExecution ex) {
        if (log.isInfoEnabled()) {
            log.info(httpLoggingInterceptorUtil.getRequestDetailContent(request, requestBody, webServiceName));
        }
        ClientHttpResponse response;
        long startTime = System.currentTimeMillis();
        try {
            response = ex.execute(request, requestBody);
            if (log.isInfoEnabled()) {
                HttpResponseWrapper responseWrapper = new HttpResponseWrapper(response);
                log.info(httpLoggingInterceptorUtil.getResponseDetailContent(responseWrapper, webServiceName,
                        System.currentTimeMillis() - startTime));
                return responseWrapper;
            } else {
                return response;
            }
        } catch (IOException e) {
            if (log.isInfoEnabled()) {
                log.info(httpLoggingInterceptorUtil.getExceptionDetailContent(e, webServiceName,
                        System.currentTimeMillis() - startTime));
            }
            throw new EcommerceException(ECOMErrorType.GENERAL_ERROR);
        }
    }
}
