package com.telegram.ecommerce.application.advice;

import lombok.Data;

import java.util.Map;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
@Data
public class ExceptionParam {
    private String errorCode;
    private String message;
    private Map<String, Object> errorParams;
}
