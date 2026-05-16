package com.spendsmart.recurring.exception;

public class RecurringException extends RuntimeException {
    
    public RecurringException(String message) {
        super(message);
    }
    
    public RecurringException(String message, Throwable cause) {
        super(message, cause);
    }
}
