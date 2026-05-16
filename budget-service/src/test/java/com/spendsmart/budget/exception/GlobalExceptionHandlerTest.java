package com.spendsmart.budget.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleBudgetException() {
        BudgetException ex = new BudgetException("Test budget exception");
        org.springframework.web.context.request.WebRequest request = mock(org.springframework.web.context.request.WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleBudgetException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Test budget exception");
        assertThat(response.getBody().getPath()).isEqualTo("uri=/test");
    }

    public void dummyMethod(String param) {}

    @Test
    void handleValidationException() throws NoSuchMethodException {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "must not be null");
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));
        
        java.lang.reflect.Method method = this.getClass().getMethod("dummyMethod", String.class);
        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(method, 0);
        MethodArgumentNotValidException ex = new MethodArgumentNotValidException(methodParameter, bindingResult);

        org.springframework.web.context.request.WebRequest request = mock(org.springframework.web.context.request.WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleValidationException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getValidationErrors()).containsEntry("field", "must not be null");
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException ex = new IllegalArgumentException("Test illegal argument");
        org.springframework.web.context.request.WebRequest request = mock(org.springframework.web.context.request.WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleIllegalArgumentException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Test illegal argument");
    }

    @Test
    void handleGlobalException() {
        Exception ex = new Exception("Test general exception");
        org.springframework.web.context.request.WebRequest request = mock(org.springframework.web.context.request.WebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/test");

        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response = handler.handleGlobalException(ex, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred. Please try again later.");
    }
}
