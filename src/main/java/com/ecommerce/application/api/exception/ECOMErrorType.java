package com.ecommerce.application.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * @author reza gholamzad
 * @since 6/11/26
 */
@Getter
@RequiredArgsConstructor
public enum ECOMErrorType {

    GENERAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.general"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "error.validation"),
    INVALID_TICKET(HttpStatus.BAD_REQUEST, "error.invalid.ticket"),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "error.invalid.password"),
    INVALID_SIGNUP_TOKEN(HttpStatus.BAD_REQUEST, "error.invalid.signup.token"),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "error.user.not.found"),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "error.user.already.exists"),
    SEND_TICKET_TIME_LIMIT(HttpStatus.TOO_MANY_REQUESTS, "error.ticket.send.time.limit"),
    TICKET_BLOCKED(HttpStatus.FORBIDDEN, "error.ticket.blocked"),
    TOO_MANY_REQUEST(HttpStatus.TOO_MANY_REQUESTS, "error.too.many.requests"),
    SMS_SEND_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "error.sms.send.failed"),
    JSON_CONVERSION_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "error.json.conversion");

    private final HttpStatus httpStatus;
    private final String messageKey;
}
