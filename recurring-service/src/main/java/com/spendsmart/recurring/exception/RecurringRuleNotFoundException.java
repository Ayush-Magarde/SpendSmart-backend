package com.spendsmart.recurring.exception;

public class RecurringRuleNotFoundException extends RecurringException {
    
    public RecurringRuleNotFoundException(Long id) {
        super("Recurring rule not found with id: " + id);
    }
    
    public RecurringRuleNotFoundException(String message) {
        super(message);
    }
}
