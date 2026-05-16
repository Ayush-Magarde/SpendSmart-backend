package com.spendsmart.auth.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handleRuntime() {
        RuntimeException ex = new RuntimeException("Test runtime");
        ResponseEntity<Map<String, Object>> res = handler.handleRuntime(ex);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(res.getBody().get("error")).isEqualTo("Test runtime");
    }

    @Test
    void handleSecurity() {
        SecurityException ex = new SecurityException("Test security");
        ResponseEntity<Map<String, Object>> res = handler.handleSecurity(ex);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(res.getBody().get("error")).isEqualTo("Test security");
    }

    @Test
    void handleGeneral() {
        Exception ex = new Exception("Test general");
        ResponseEntity<Map<String, Object>> res = handler.handleGeneral(ex);

        assertThat(res.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(res.getBody().get("error")).isEqualTo("Something went wrong");
    }
}
