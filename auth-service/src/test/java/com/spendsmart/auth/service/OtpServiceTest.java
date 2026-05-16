package com.spendsmart.auth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.auth.dto.OtpData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OtpServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;
    
    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private OtpService otpService;

    @BeforeEach
    void setUp() {
        // leniency for opsForValue which might not be called in every test
        org.mockito.Mockito.lenient().when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void generateAndStoreOtp() throws Exception {
        when(redisTemplate.hasKey("otp:test@test.com")).thenReturn(false);
        when(objectMapper.writeValueAsString(any(OtpData.class))).thenReturn("json");

        String otp = otpService.generateAndStoreOtp("Test", "test@test.com", "pass");
        
        assertThat(otp).isNotNull().hasSize(6);
        verify(valueOperations).set(eq("otp:test@test.com"), eq("json"), eq(5L), eq(TimeUnit.MINUTES));
    }

    @Test
    void generateAndStoreOtp_AlreadyExists() {
        when(redisTemplate.hasKey("otp:test@test.com")).thenReturn(true);

        assertThatThrownBy(() -> otpService.generateAndStoreOtp("Test", "test@test.com", "pass"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("OTP already sent");
    }

    @Test
    void verifyOtp_Success() throws Exception {
        OtpData data = new OtpData("Test", "test@test.com", "pass", "123456", null, 0);
        
        when(valueOperations.get("otp:test@test.com")).thenReturn("json");
        when(objectMapper.readValue("json", OtpData.class)).thenReturn(data);

        OtpData result = otpService.verifyOtp("test@test.com", "123456");

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@test.com");
    }

    @Test
    void verifyOtp_NotFound() {
        when(valueOperations.get("otp:nonexistent@test.com")).thenReturn(null);
        
        assertThatThrownBy(() -> otpService.verifyOtp("nonexistent@test.com", "123456"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void verifyOtp_Invalid() throws Exception {
        OtpData data = new OtpData("Test", "test@test.com", "pass", "123456", null, 0);
        
        when(valueOperations.get("otp:test@test.com")).thenReturn("json");
        when(objectMapper.readValue("json", OtpData.class)).thenReturn(data);
        when(objectMapper.writeValueAsString(any(OtpData.class))).thenReturn("json-updated");
        when(redisTemplate.getExpire("otp:test@test.com", TimeUnit.SECONDS)).thenReturn(300L);

        assertThatThrownBy(() -> otpService.verifyOtp("test@test.com", "wrong"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Invalid OTP");
                
        verify(valueOperations).set(eq("otp:test@test.com"), eq("json-updated"), anyLong(), eq(TimeUnit.SECONDS));
    }

    @Test
    void verifyOtp_TooManyAttempts() throws Exception {
        OtpData data = new OtpData("Test", "test@test.com", "pass", "123456", null, 3); // 3 attempts
        
        when(valueOperations.get("otp:test@test.com")).thenReturn("json");
        when(objectMapper.readValue("json", OtpData.class)).thenReturn(data);

        assertThatThrownBy(() -> otpService.verifyOtp("test@test.com", "123456"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Too many failed attempts");
                
        verify(redisTemplate).delete("otp:test@test.com");
    }

    @Test
    void removeOtp() {
        otpService.removeOtp("test@test.com");
        verify(redisTemplate).delete("otp:test@test.com");
    }
}
