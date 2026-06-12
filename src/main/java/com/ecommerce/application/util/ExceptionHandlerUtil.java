package com.ecommerce.application.util;

import com.ecommerce.application.advice.ExceptionParam;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author AmirHossein ZamanZade
 * @since 4/25/2023
 */
@Component
@RequiredArgsConstructor
public class ExceptionHandlerUtil {

    private final ObjectMapper objectMapper;

    public ExceptionParam generateExceptionParam(Throwable exception) {
        if (exception instanceof EcommerceException e) {
            Map<String, Object> exceptionParams = convertToExceptionParam(exception);
            return generateExceptionParam(e.getEcomErrorType(), exceptionParams);
        }
        return handleThrowable(exception);
    }

    public <T> Map<String, Object> convertToExceptionParam(T object) {
        return jsonToMap(objectToJsonForRestException(object));
    }

    private Map<String, Object> jsonToMap(String jsonString) {
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
            };
            return objectMapper.readValue(jsonString, typeRef);
        } catch (Exception e) {
            throw new EcommerceException(ECOMErrorType.JSON_CONVERSION_ERROR);
        }
    }

    private <T> String objectToJsonForRestException(T object) {
        try {
            return objectMapper.writer().writeValueAsString(object);
        } catch (Exception e) {
            throw new EcommerceException(ECOMErrorType.JSON_CONVERSION_ERROR);
        }
    }

    private ExceptionParam handleThrowable(Throwable throwable) {
        return generateExceptionParam(ECOMErrorType.GENERAL_ERROR, null);
    }

    private ExceptionParam generateExceptionParam(ECOMErrorType errorType, Map<String, Object> errorParams) {
        ExceptionParam exceptionParam = new ExceptionParam();
        exceptionParam.setMessage(errorType.getMessageKey());
        exceptionParam.setErrorCode(errorType.name());
        exceptionParam.setErrorParams(errorParams);
        return exceptionParam;
    }
}
