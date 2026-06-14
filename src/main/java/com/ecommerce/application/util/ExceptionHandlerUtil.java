package com.ecommerce.application.util;

import com.ecommerce.application.advice.ExceptionParam;
import com.ecommerce.application.api.exception.ECOMErrorType;
import com.ecommerce.application.api.exception.EcommerceException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author AmirHossein ZamanZade
 * @since 4/25/2023
 */
@Component
@RequiredArgsConstructor
public class ExceptionHandlerUtil {

    private final MessageSource messageSource;

    public ExceptionParam generateExceptionParam(Throwable exception) {
        if (exception instanceof EcommerceException e) {
            return buildParam(e.getEcomErrorType(), e.getData());
        }
        return buildParam(ECOMErrorType.GENERAL_ERROR, null);
    }

    private ExceptionParam buildParam(ECOMErrorType errorType, Map<String, Object> data) {
        var param = new ExceptionParam();
        param.setErrorCode(errorType.name());
        param.setMessage(resolveMessage(errorType));
        param.setErrorParams(data);
        return param;
    }

    private String resolveMessage(ECOMErrorType errorType) {
        try {
            return messageSource.getMessage(errorType.getMessageKey(), null, LocaleContextHolder.getLocale());
        } catch (NoSuchMessageException e) {
            return errorType.getMessageKey();
        }
    }
}
