package com.ecommerce.application.api.exception;

public class EcommerceException extends Exception {
    public EcommerceException(String message) {
        super(message);
    }

    public String getErrorCode() {
        return this.getClass().getSimpleName();
    }

}
