package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private EmailService emailService;
    @Mock
    private OtpService otpService;
    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "adminEmail", "admin@spendsmart.com");
    }

    @Test
    void register_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass");
        req.setName("Test");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setName("Test");
        when(userRepository.save(any())).thenReturn(user);

        when(jwtUtil.generateToken("test@test.com", null, 1L)).thenReturn("token");

        AuthResponse res = authService.register(req);

        assertThat(res.getToken()).isEqualTo("token");
        verify(emailService, times(2)).sendEmail(anyString(), anyString(), anyString());
        // Verify RabbitMQ event instead of Feign client
        verify(rabbitTemplate).convertAndSend(eq("notification.exchange"), eq("notification.routing.key"), any(NotificationEvent.class));
    }

    @Test
    void register_AdminSuccess() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("admin@spendsmart.com");
        req.setPassword("pass");
        req.setName("Admin");

        when(userRepository.findByEmail("admin@spendsmart.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");

        User user = new User();
        user.setId(2L);
        user.setEmail("admin@spendsmart.com");
        user.setName("Admin");
        user.setRole("ADMIN");
        when(userRepository.save(argThat(u -> "ADMIN".equals(u.getRole())))).thenReturn(user);

        when(jwtUtil.generateToken("admin@spendsmart.com", "ADMIN", 2L)).thenReturn("admin_token");

        AuthResponse res = authService.register(req);

        assertThat(res.getToken()).isEqualTo("admin_token");
    }

    @Test
    void register_UserExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("User already exists");
    }

    @Test
    void login_Success() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("encoded");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(true);
        when(jwtUtil.generateToken("test@test.com", null, 1L)).thenReturn("token");

        AuthResponse res = authService.login(req);

        assertThat(res.getToken()).isEqualTo("token");
    }

    @Test
    void login_InvalidPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("test@test.com");
        req.setPassword("pass");

        User user = new User();
        user.setPassword("encoded");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("pass", "encoded")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Invalid password");
    }

    @Test
    void initiateRegistration_Success() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");
        req.setName("Test");
        req.setPassword("pass");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(otpService.generateAndStoreOtp("Test", "test@test.com", "encoded")).thenReturn("123456");

        authService.initiateRegistration(req);

        verify(emailService).sendEmail(eq("test@test.com"), anyString(), contains("123456"));
    }

    @Test
    void initiateRegistration_UserExists() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("test@test.com");

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(new User()));

        assertThatThrownBy(() -> authService.initiateRegistration(req))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void verifyRegistration_Success() {
        OtpData data = new OtpData("Test", "test@test.com", "encoded", "123456", null, 0);
        when(otpService.verifyOtp("test@test.com", "123456")).thenReturn(data);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        when(userRepository.save(any())).thenReturn(user);
        when(jwtUtil.generateToken(any(), any(), any())).thenReturn("token");

        AuthResponse res = authService.verifyRegistration("test@test.com", "123456");

        assertThat(res.getToken()).isEqualTo("token");
        verify(otpService).removeOtp("test@test.com");
        // Verify RabbitMQ event
        verify(rabbitTemplate).convertAndSend(eq("notification.exchange"), eq("notification.routing.key"), any(NotificationEvent.class));
    }

    @Test
    void verifyRegistration_AdminSuccess() {
        OtpData data = new OtpData("Admin", "admin@spendsmart.com", "encoded", "123456", null, 0);
        when(otpService.verifyOtp("admin@spendsmart.com", "123456")).thenReturn(data);

        User user = new User();
        user.setId(2L);
        user.setEmail("admin@spendsmart.com");
        user.setRole("ADMIN");
        when(userRepository.save(argThat(u -> "ADMIN".equals(u.getRole())))).thenReturn(user);
        when(jwtUtil.generateToken(eq("admin@spendsmart.com"), eq("ADMIN"), eq(2L))).thenReturn("admin_token");

        AuthResponse res = authService.verifyRegistration("admin@spendsmart.com", "123456");

        assertThat(res.getToken()).isEqualTo("admin_token");
        verify(otpService).removeOtp("admin@spendsmart.com");
        // Verify RabbitMQ event
        verify(rabbitTemplate).convertAndSend(eq("notification.exchange"), eq("notification.routing.key"), any(NotificationEvent.class));
    }

    @Test
    void upgradeUser_Success() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setName("Test");
        user.setPremium(false);

        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userRepository.save(any())).thenReturn(user);

        authService.upgradeUser("test@test.com");

        assertThat(user.isPremium()).isTrue();
        verify(userRepository).save(user);
        verify(rabbitTemplate).convertAndSend(eq("notification.exchange"), eq("notification.routing.key"), any(NotificationEvent.class));
    }
}
