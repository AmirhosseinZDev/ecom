package com.ecommerce.application.invoker.sms.client;

import com.ecommerce.application.invoker.sms.dto.SmsRequestDto;
import com.ecommerce.application.invoker.sms.dto.SmsResponseDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * @author AmirHossein ZamanZade
 * @since 5/2/26
 */
@HttpExchange(
        accept = MediaType.APPLICATION_JSON_VALUE,
        contentType = MediaType.APPLICATION_JSON_VALUE
)
public interface SmsClient {

    @PostExchange("/v1/send/verify")
    SmsResponseDto sendMessage(@RequestBody SmsRequestDto smsRequestDto);
}
