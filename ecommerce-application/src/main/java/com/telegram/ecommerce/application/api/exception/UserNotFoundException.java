package com.telegram.ecommerce.application.api.exception;

/**
 * @author AmirHossein ZamanZade
 * @since 5/10/26
 */
public class UserNotFoundException extends EcommerceException {

    public UserNotFoundException(String message) {
        super(message);
    }
}
