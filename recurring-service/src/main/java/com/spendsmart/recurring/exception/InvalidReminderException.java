package com.spendsmart.recurring.exception;

public class InvalidReminderException extends RecurringException {
    
    public InvalidReminderException(String message) {
        super(message);
    }
    
    public InvalidReminderException(String message, Throwable cause) {
        super(message, cause);
    }
}
