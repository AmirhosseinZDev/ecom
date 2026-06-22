package com.ecommerce.application.invoker.sms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigDecimal;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@Getter
@Setter
public class SmsResponseDto {

    @JsonProperty("status")
    private Integer status;

    @JsonProperty("message")
    private String message;

    @JsonProperty("data")
    private SmsData data;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SmsData {

        @JsonProperty("messageId")
        private Long messageId;

        @JsonProperty("cost")
        private BigDecimal cost;
    }
}
