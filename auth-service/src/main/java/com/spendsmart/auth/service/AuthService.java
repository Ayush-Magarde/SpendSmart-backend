package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.*;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    void initiateRegistration(RegisterRequest request);

    AuthResponse verifyRegistration(String email, String otp);

    void upgradeUser(String email);
}