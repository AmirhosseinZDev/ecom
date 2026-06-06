package com.ecommerce.application.api.exception;

public class EcommerceServiceException extends RuntimeException {

    public EcommerceServiceException(String message) {
        super(message);
    }

    public EcommerceServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return this.getClass().getSimpleName();
    }

}
