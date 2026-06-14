package com.ecommerce.application.advice;

import com.ecommerce.application.api.exception.EcommerceException;
import com.ecommerce.application.util.ExceptionHandlerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * @author AmirHossein ZamanZade
 * @since 4/18/25
 */
@RestControllerAdvice
@RequiredArgsConstructor
public class EcommerceControllerAdvice extends ResponseEntityExceptionHandler {

    private final ExceptionHandlerUtil exceptionHandlerUtil;

    @ExceptionHandler(EcommerceException.class)
    public ExceptionParam handleEcommerceException(EcommerceException exception,
            jakarta.servlet.http.HttpServletResponse response) {
        response.setStatus(exception.getEcomErrorType().getHttpStatus().value());
        return exceptionHandlerUtil.generateExceptionParam(exception);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionParam handleThrowable(Throwable throwable) {
        return exceptionHandlerUtil.generateExceptionParam(throwable);
    }

    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionParam handleAccessDeniedException(AccessDeniedException exception) {
        return exceptionHandlerUtil.generateExceptionParam(exception);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionParam handleAuthenticationException(AuthenticationException exception) {
        return exceptionHandlerUtil.generateExceptionParam(exception);
    }

    @ExceptionHandler(AuthenticationServiceException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionParam handleAuthenticationServiceException(AuthenticationServiceException exception) {
        return exceptionHandlerUtil.generateExceptionParam(exception);
    }
}
