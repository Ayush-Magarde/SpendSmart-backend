package com.spendsmart.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpData {
    private String name;
    private String email;
    private String password; // encoded password
    private String otp;
    private LocalDateTime expiryTime;
    private int attempts; // NEW: Track failed attempts
}
