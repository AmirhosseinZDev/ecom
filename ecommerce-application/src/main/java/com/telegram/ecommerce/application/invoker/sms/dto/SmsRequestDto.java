package com.telegram.ecommerce.application.invoker.sms.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * @author AmirHossein ZamanZade
 * @since 5/2/26
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class SmsRequestDto {

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("templateId")
    private Integer templateId;

    @JsonProperty("parameters")
    private List<Parameter> parameters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Parameter {

        @JsonProperty("name")
        private String name;

        @JsonProperty("value")
        private String value;
    }
}
