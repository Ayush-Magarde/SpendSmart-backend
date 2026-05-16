package com.spendsmart.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.auth.dto.OtpData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtpService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    
    private static final String OTP_PREFIX = "otp:";
    private static final long OTP_TTL_MINUTES = 5;

    /**
     * Generate and store OTP for registration
     */
    public String generateAndStoreOtp(String name, String email, String password) {
        String key = OTP_PREFIX + email;
        
        // Check if OTP already exists
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key))) {
            throw new RuntimeException("OTP already sent. Please wait or verify.");
        }
        
        // Generate 6-digit numeric OTP
        String otp = String.format("%06d", new Random().nextInt(1000000));
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(OTP_TTL_MINUTES);
        
        OtpData otpData = new OtpData(name, email, password, otp, expiryTime, 0);
        
        try {
            String json = objectMapper.writeValueAsString(otpData);
            redisTemplate.opsForValue().set(key, json, OTP_TTL_MINUTES, TimeUnit.MINUTES);
            log.info("Generated and cached OTP for email: {}, expires in {} minutes", email, OTP_TTL_MINUTES);
        } catch (Exception e) {
            log.error("Failed to serialize and cache OTP data", e);
            throw new RuntimeException("Internal error while processing OTP");
        }
        
        return otp;
    }

    /**
     * Verify OTP and return stored data if valid
     */
    public OtpData verifyOtp(String email, String otp) {
        String key = OTP_PREFIX + email;
        String json = redisTemplate.opsForValue().get(key);
        
        if (json == null) {
            throw new RuntimeException("OTP not found or expired for email: " + email);
        }
        
        try {
            OtpData otpData = objectMapper.readValue(json, OtpData.class);
            
            // Check attempt limit
            if (otpData.getAttempts() >= 3) {
                redisTemplate.delete(key);
                throw new RuntimeException("Too many failed attempts. Please request a new OTP.");
            }
            
            // Check OTP match
            if (!otp.equals(otpData.getOtp())) {
                otpData.setAttempts(otpData.getAttempts() + 1);
                String updatedJson = objectMapper.writeValueAsString(otpData);
                // Preserve remaining TTL
                Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
                if (expire != null && expire > 0) {
                    redisTemplate.opsForValue().set(key, updatedJson, expire, TimeUnit.SECONDS);
                }
                throw new RuntimeException("Invalid OTP for email: " + email);
            }
            
            log.info("OTP verified successfully for email: {}", email);
            return otpData;
        } catch (RuntimeException re) {
            throw re; // re-throw business exceptions
        } catch (Exception e) {
            log.error("Error processing cached OTP data", e);
            throw new RuntimeException("Internal error verifying OTP");
        }
    }

    /**
     * Remove OTP from store
     */
    public void removeOtp(String email) {
        String key = OTP_PREFIX + email;
        redisTemplate.delete(key);
        log.info("OTP removed for email: {}", email);
    }
}
