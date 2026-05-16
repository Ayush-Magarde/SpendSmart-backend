package com.spendsmart.payment.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private WebRequest mockRequest() {
        return new ServletWebRequest(new MockHttpServletRequest());
    }

    @Test
    void handlePaymentException_returns400() {
        PaymentException ex = new PaymentException("Payment failed");

        ResponseEntity<Map<String, Object>> response = handler.handlePaymentException(ex, mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("message", "Payment failed");
        assertThat(response.getBody()).containsEntry("error", "Payment Error");
    }

    @Test
    void handlePaymentExceptionWithCause_returns400() {
        PaymentException ex = new PaymentException("Payment failed", new RuntimeException("cause"));

        ResponseEntity<Map<String, Object>> response = handler.handlePaymentException(ex, mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("message", "Payment failed");
    }

    public void dummyMethod(String param) {}

    @Test
    void handleValidationException_returns400() throws NoSuchMethodException {
        org.springframework.validation.BindingResult bindingResult = mock(org.springframework.validation.BindingResult.class);
        org.springframework.validation.FieldError fieldError = new org.springframework.validation.FieldError("object", "field", "must not be null");
        when(bindingResult.getAllErrors()).thenReturn(java.util.List.of(fieldError));
        
        java.lang.reflect.Method method = this.getClass().getMethod("dummyMethod", String.class);
        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(method, 0);
        org.springframework.web.bind.MethodArgumentNotValidException ex = new org.springframework.web.bind.MethodArgumentNotValidException(methodParameter, bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidationException(ex, mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("error", "Validation Error");
        assertThat((Map<String, String>) response.getBody().get("validationErrors")).containsEntry("field", "must not be null");
    }

    @Test
    void handleGlobalException_returns500() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<Map<String, Object>> response = handler.handleGlobalException(ex, mockRequest());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(response.getBody()).containsEntry("error", "Internal Server Error");
    }
}
