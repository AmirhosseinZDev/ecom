package com.ecommerce.application.api.exception;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

/**
 * @author AmirHossein ZamanZade
 * @since 5/2/26
 */
@Getter
@EqualsAndHashCode(callSuper = false)
@ToString
public class EcommerceException extends RuntimeException {

    private final ECOMErrorType ecomErrorType;
    private final Map<String, Object> data;
    private final Object[] messageArgs;

    public EcommerceException(ECOMErrorType ecomErrorType) {
        this(ecomErrorType, null, (Object[]) null);
    }

    public EcommerceException(ECOMErrorType ecomErrorType, Object... messageArgs) {
        this.ecomErrorType = ecomErrorType;
        this.messageArgs = messageArgs;
        this.data = null;
    }

    public EcommerceException(ECOMErrorType ecomErrorType, Map<String, Object> data, Object[] messageArgs) {
        this.ecomErrorType = ecomErrorType;
        this.data = data;
        this.messageArgs = messageArgs;
    }

    public EcommerceException(ECOMErrorType ecomErrorType, Map<String, Object> data) {
        this.ecomErrorType = ecomErrorType;
        this.data = data;
        this.messageArgs = null;
    }

    @Override
    public String getMessage() {
        return ecomErrorType.getMessageKey();
    }
}
