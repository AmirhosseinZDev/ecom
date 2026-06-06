package com.ecommerce.application.api.exception;

import com.tosan.validation.util.ValidationViolationInfo;

import java.util.List;
import java.util.Map;

/**
 * @author AmirHossein ZamanZade
 * @since 3/24/25
 */
public class ValidationException extends EcommerceServiceException {

    private Map<String, List<ValidationViolationInfo>> validationViolationInfo;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String message, Map<String, List<ValidationViolationInfo>> validationViolationInfo) {
        super(message);
        this.validationViolationInfo = validationViolationInfo;
    }

    public Map<String, List<ValidationViolationInfo>> getValidationViolationInfo() {
        return validationViolationInfo;
    }
}
