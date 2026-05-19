package com.telegram.ecommerce.application.api.exception;

public class MismatchNationalCodeWithMobileNumberException extends EcommerceException {
    public MismatchNationalCodeWithMobileNumberException(String message) {
        super(message);
    }
}
