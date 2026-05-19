package com.telegram.ecommerce.application.api.exception;

public class InvalidSignupTokenException extends EcommerceException {
    public InvalidSignupTokenException(String message) {
        super(message);
    }
}
