package com.telegram.ecommerce.application.invoker.shahkar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@Getter
@Setter
public class ShahkarResponseDto {

    @JsonProperty("response_body")
    private ResponseBody responseBody;

    @JsonProperty("result")
    private Integer result;

    @Getter
    @Setter
    public static class ResponseBody {

        @JsonProperty("data")
        private Data data;

        @JsonProperty("error_code")
        private String errorCode;

        @JsonProperty("message")
        private String message;
    }

    @Getter
    @Setter
    public static class Data {

        @JsonProperty("matched")
        private Boolean matched;
    }
}
