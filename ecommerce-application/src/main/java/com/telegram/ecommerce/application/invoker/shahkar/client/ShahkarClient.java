package com.telegram.ecommerce.application.invoker.shahkar.client;

import com.telegram.ecommerce.application.invoker.shahkar.dto.ShahkarRequestDto;
import com.telegram.ecommerce.application.invoker.shahkar.dto.ShahkarResponseDto;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@HttpExchange(
        accept = MediaType.APPLICATION_JSON_VALUE,
        contentType = MediaType.APPLICATION_JSON_VALUE
)
public interface ShahkarClient {

    @PostExchange("/services/inquiry/shahkar")
    ShahkarResponseDto match(@RequestBody ShahkarRequestDto requestDto);
}
