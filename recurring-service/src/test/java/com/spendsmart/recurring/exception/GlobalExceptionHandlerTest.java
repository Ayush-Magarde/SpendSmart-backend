package com.spendsmart.recurring.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRecurringRuleNotFound() {
        RecurringRuleNotFoundException ex = new RecurringRuleNotFoundException("Test");
        ResponseEntity<Map<String, Object>> response = handler.handleRecurringRuleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).isEqualTo("Test");
    }

    @Test
    void handleReminderNotFound() {
        ReminderNotFoundException ex = new ReminderNotFoundException("Test");
        ResponseEntity<Map<String, Object>> response = handler.handleReminderNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody().get("message")).isEqualTo("Test");
    }

    @Test
    void handleInvalidReminder() {
        InvalidReminderException ex = new InvalidReminderException("Test");
        ResponseEntity<Map<String, Object>> response = handler.handleInvalidReminder(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("Test");
    }

    @Test
    void handleRecurringException() {
        RecurringException ex = new RecurringException("Test");
        ResponseEntity<Map<String, Object>> response = handler.handleRecurringException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("message")).isEqualTo("Test");
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Test");
        ResponseEntity<Map<String, Object>> response = handler.handleIllegalArgumentException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message")).isEqualTo("Test");
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Test");
        ResponseEntity<Map<String, Object>> response = handler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().get("message")).isEqualTo("An unexpected error occurred. Please try again later.");
    }
}
