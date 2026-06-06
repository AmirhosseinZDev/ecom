package com.ecommerce.application.api.exception;

public class SendTicketTimeLimitNotExceededException extends RuntimeException {
    public SendTicketTimeLimitNotExceededException(String message) {
        super(message);
    }
}
