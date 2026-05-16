package com.spendsmart.auth.controller;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;


    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    // NEW: OTP-based registration endpoints

    /**
     * Initiate registration with OTP verification
     */
    @PostMapping("/register/init")
    public String initiateRegistration(@RequestBody RegisterRequest request) {
        authService.initiateRegistration(request);
        return "OTP sent successfully";
    }

    /**
     * Verify OTP and complete registration
     */
    @PostMapping("/register/verify")
    public AuthResponse verifyRegistration(@RequestBody OtpVerificationRequest request) {
        return authService.verifyRegistration(request.getEmail(), request.getOtp());
    }
}