package com.ecommerce.application.api.exception;

/**
 * @author AmirHossein ZamanZade
 * @since 12/26/25
 */
public class UserHasAlreadyExistException extends EcommerceException {

    public UserHasAlreadyExistException(String message) {
        super(message);
    }
}
