package com.spendsmart.summary.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntime_returnsBadRequest() {
        RuntimeException ex = new RuntimeException("Bad Request Error");
        ResponseEntity<Map<String, Object>> response = handler.handleRuntime(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody()).containsEntry("message", "Bad Request Error");
    }

    @Test
    void handleSecurity_returnsUnauthorized() {
        SecurityException ex = new SecurityException("Unauthorized Access");
        ResponseEntity<Map<String, Object>> response = handler.handleSecurity(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("status", 401);
        assertThat(response.getBody()).containsEntry("message", "Unauthorized Access");
    }

    public void dummyMethod(String param) {}

    @Test
    void handleMissingHeader_returnsBadRequest() throws NoSuchMethodException {
        java.lang.reflect.Method method = this.getClass().getMethod("dummyMethod", String.class);
        org.springframework.core.MethodParameter methodParameter = new org.springframework.core.MethodParameter(method, 0);
        org.springframework.web.bind.MissingRequestHeaderException ex = new org.springframework.web.bind.MissingRequestHeaderException("X-User-Email", methodParameter);
        ResponseEntity<Map<String, Object>> response = handler.handleMissingHeader(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).containsEntry("status", 400);
        assertThat(response.getBody().get("message").toString()).contains("Required request header 'X-User-Email'");
    }

    @Test
    void handleGeneral_returnsInternalServerError() {
        Exception ex = new Exception("General Error");
        ResponseEntity<Map<String, Object>> response = handler.handleGeneral(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", 500);
        assertThat(response.getBody()).containsEntry("message", "Something went wrong");
    }
}
