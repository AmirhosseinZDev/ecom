package com.telegram.ecommerce.application.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.ecommerce.application.advice.ExceptionParam;
import com.telegram.ecommerce.application.api.exception.EcommerceException;
import com.telegram.ecommerce.application.api.exception.EcommerceServiceException;
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
        Map<String, Object> exceptionParams;
        if (exception instanceof EcommerceException e) {
            exceptionParams = convertToExceptionParam(exception);
            return generateExceptionParam(e.getMessage(), e.getErrorCode(), exceptionParams);
        } else if (exception instanceof EcommerceServiceException e) {
            exceptionParams = convertToExceptionParam(exception);
            return generateExceptionParam(e.getMessage(), e.getErrorCode(), exceptionParams);
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
            throw new EcommerceServiceException("error in converting Json to map JSONObject", e);
        }
    }

    private <T> String objectToJsonForRestException(T object) {
        try {
            return objectMapper.writer().writeValueAsString(object);
        } catch (Exception e) {
            throw new EcommerceServiceException("error in converting object to Json", e);
        }
    }

    private ExceptionParam handleThrowable(Throwable throwable) {
        EcommerceServiceException serviceException = new EcommerceServiceException(throwable.getMessage());
        return generateExceptionParam(serviceException.getMessage(), serviceException.getErrorCode(),
                null);
    }

    private ExceptionParam generateExceptionParam(String message, String errorCode, Map<String, Object> errorParams) {
        ExceptionParam exceptionParam = new ExceptionParam();
        exceptionParam.setMessage(message);
        exceptionParam.setErrorCode(errorCode);
        exceptionParam.setErrorParams(errorParams);
        return exceptionParam;
    }
}
