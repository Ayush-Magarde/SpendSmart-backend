package com.spendsmart.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.auth.dto.AuthResponse;
import com.spendsmart.auth.dto.LoginRequest;
import com.spendsmart.auth.dto.OtpVerificationRequest;
import com.spendsmart.auth.dto.RegisterRequest;
import com.spendsmart.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@org.springframework.context.annotation.Import({
    com.spendsmart.auth.config.SecurityConfig.class,
    com.spendsmart.auth.security.JwtAuthFilter.class,
    com.spendsmart.auth.security.OAuth2SuccessHandler.class,
    com.spendsmart.auth.exception.GlobalExceptionHandler.class
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;
    
    @MockBean
    private com.spendsmart.auth.security.JwtUtil jwtUtil;
    
    @MockBean
    private com.spendsmart.auth.repository.UserRepository userRepository;

    @Test
    void login() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass");

        AuthResponse res = new AuthResponse("token", "test@test.com", "Test", false, "USER");
        when(authService.login(any())).thenReturn(res);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void initiateRegistration() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass");

        doNothing().when(authService).initiateRegistration(any());

        mockMvc.perform(post("/api/auth/register/init")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void verifyRegistration() throws Exception {
        OtpVerificationRequest req = new OtpVerificationRequest();
        req.setEmail("test@test.com");
        req.setOtp("123456");

        AuthResponse res = new AuthResponse("token", "test@test.com", "Test", false, "USER");
        when(authService.verifyRegistration(any(), any())).thenReturn(res);

        mockMvc.perform(post("/api/auth/register/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }
}
