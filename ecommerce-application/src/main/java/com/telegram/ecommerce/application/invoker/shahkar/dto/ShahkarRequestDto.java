package com.telegram.ecommerce.application.invoker.shahkar.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author AmirHossein ZamanZade
 * @since 5/5/26
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ShahkarRequestDto {

    @JsonProperty("mobile")
    private String mobile;

    @JsonProperty("national_code")
    private String nationalCode;
}
