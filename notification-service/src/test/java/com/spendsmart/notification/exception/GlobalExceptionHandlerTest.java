package com.spendsmart.notification.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleNotificationException_ReturnsBadRequest() {
        NotificationException ex = new NotificationException("Test error");
        WebRequest request = mock(WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<Map<String, Object>> response = handler.handleNotificationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Test error");
        assertThat(response.getBody()).containsEntry("path", "uri=/test");
    }

    @Test
    void handleValidationException_ReturnsBadRequestWithErrors() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        
        java.lang.reflect.Method method = this.getClass().getMethod("dummyMethod", String.class);
        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Validation Error");
        assertThat((Map<String, String>) response.getBody().get("validationErrors")).containsEntry("field", "must not be null");
    }

    public void dummyMethod(String param) {}

    @Test
    void handleMissingHeader_ReturnsBadRequest() throws NoSuchMethodException {
        java.lang.reflect.Method method = this.getClass().getMethod("dummyMethod", String.class);
        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(method, 0);
        org.springframework.web.bind.MissingRequestHeaderException ex = new org.springframework.web.bind.MissingRequestHeaderException("X-User-Email", methodParameter);
        
        WebRequest request = mock(WebRequest.class);
        ResponseEntity<Map<String, Object>> response = handler.handleMissingHeader(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString()).contains("Required request header 'X-User-Email'");
    }

    @Test
    void handleGlobalException_ReturnsInternalServerError() {
        Exception ex = new Exception("Unexpected error");
        WebRequest request = mock(WebRequest.class);

        ResponseEntity<Map<String, Object>> response = handler.handleGlobalException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("message", "An unexpected error occurred. Please try again later.");
    }
}
