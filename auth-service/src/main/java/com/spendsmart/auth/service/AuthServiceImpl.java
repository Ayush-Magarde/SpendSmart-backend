package com.spendsmart.auth.service;

import com.spendsmart.auth.dto.*;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import com.spendsmart.auth.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final OtpService otpService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Value("${admin.email}")
    private String adminEmail;

    @Override
    public AuthResponse register(RegisterRequest request) {

        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        // Create user
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        
        if (request.getEmail().equalsIgnoreCase(adminEmail)) {
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }
        
        user.setProvider("LOCAL");

        // Save user
        User savedUser = userRepository.save(user);

        log.info("Triggering email after registration for: {}", savedUser.getEmail());

        // =========================
        // 1. ADMIN EMAIL (ALERT)
        // =========================
        try {
            emailService.sendEmail(
                    adminEmail,
                    "New User Registered",
                    "User registered: " + savedUser.getEmail()
            );
            log.info("Admin email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send admin email", e);
        }

        // =========================
        // 2. USER EMAIL (WELCOME)
        // =========================
        try {
            emailService.sendEmail(
                    savedUser.getEmail(),
                    "Welcome to SpendSmart",
                    "Hello " + savedUser.getName() + ", welcome to SpendSmart!"
            );
            log.info("User welcome email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
        }

        // =========================
        // 3. USER NOTIFICATION (FIXED)
        // =========================
        try {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("Welcome to SpendSmart");
            notificationRequest.setMessage(
                    "Hello " + savedUser.getName() + ", your account has been created successfully!"
            );
            notificationRequest.setType("INFO");
            notificationRequest.setCategory("SYSTEM");
            notificationRequest.setIsRead(false);

            // 🚀 Move to RabbitMQ
            NotificationEvent event = new NotificationEvent(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    notificationRequest
            );

            log.info("Publishing UserRegisteredEvent to RabbitMQ for: {}", savedUser.getEmail());
            rabbitTemplate.convertAndSend(
                    com.spendsmart.auth.config.RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    com.spendsmart.auth.config.RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event
            );

            log.info("User notification event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
        }

        // Generate JWT with userId
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getId()
        );

        return new AuthResponse(
                token,
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.isPremium(),
                savedUser.getRole()
        );
    }

    @Override
    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new SecurityException("Invalid password");
        }

        // Generate JWT with userId
        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole(),
                user.getId()
        );

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getName(),
                user.isPremium(),
                user.getRole()
        );
    }

    // NEW: OTP-based registration methods
    
    /**
     * Initiate registration with OTP verification
     * FIXED: Removed OTP retrieval hack - now OTP is returned directly
     */
    public void initiateRegistration(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("User already exists");
        }

        // Encode password
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        
        // Generate and store OTP - FIXED: Now returns OTP directly
        String otp = otpService.generateAndStoreOtp(request.getName(), request.getEmail(), encodedPassword);
        
        // Send OTP via email
        try {
            emailService.sendEmail(
                    request.getEmail(),
                    "SpendSmart OTP Verification",
                    "Your OTP is: " + otp // FIXED: Use returned OTP directly
            );
            log.info("OTP sent successfully to email: {}", request.getEmail());
        } catch (Exception e) {
            log.error("Failed to send OTP email", e);
            throw new RuntimeException("Failed to send OTP. Please try again.");
        }
    }

    /**
     * Verify OTP and complete registration
     */
    public AuthResponse verifyRegistration(String email, String otp) {
        // Verify OTP and get stored data
        OtpData otpData = otpService.verifyOtp(email, otp);
        
        // Create user from stored data
        User user = new User();
        user.setName(otpData.getName());
        user.setEmail(otpData.getEmail());
        user.setPassword(otpData.getPassword()); // Already encoded
        
        if (otpData.getEmail().equalsIgnoreCase(adminEmail)) {
            user.setRole("ADMIN");
        } else {
            user.setRole("USER");
        }
        
        user.setProvider("LOCAL");

        // Save user in DB
        User savedUser = userRepository.save(user);
        
        // Remove OTP from store
        otpService.removeOtp(email);

        log.info("User registered successfully: {}", savedUser.getEmail());

        // Send all existing notifications (same as original register method)
        // 1. ADMIN EMAIL (ALERT)
        try {
            emailService.sendEmail(
                    adminEmail,
                    "New User Registered",
                    "User registered: " + savedUser.getEmail()
            );
            log.info("Admin email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send admin email", e);
        }

        // 2. USER EMAIL (WELCOME)
        try {
            emailService.sendEmail(
                    savedUser.getEmail(),
                    "Welcome to SpendSmart",
                    "Hello " + savedUser.getName() + ", welcome to SpendSmart!"
            );
            log.info("User welcome email sent successfully");
        } catch (Exception e) {
            log.error("Failed to send welcome email", e);
        }

        // 3. USER NOTIFICATION
        try {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("Welcome to SpendSmart");
            notificationRequest.setMessage(
                    "Hello " + savedUser.getName() + ", your account has been created successfully!"
            );
            notificationRequest.setType("INFO");
            notificationRequest.setCategory("SYSTEM");
            notificationRequest.setIsRead(false);

            // 🚀 Move to RabbitMQ
            NotificationEvent event = new NotificationEvent(
                    savedUser.getId(),
                    savedUser.getEmail(),
                    notificationRequest
            );

            log.info("Publishing UserRegisteredEvent (via OTP) to RabbitMQ for: {}", savedUser.getEmail());
            rabbitTemplate.convertAndSend(
                    com.spendsmart.auth.config.RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    com.spendsmart.auth.config.RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event
            );

            log.info("User notification event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish notification event", e);
        }

        // Generate JWT with userId
        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole(),
                savedUser.getId()
        );

        return new AuthResponse(
                token,
                savedUser.getEmail(),
                savedUser.getName(),
                savedUser.isPremium(),
                savedUser.getRole()
        );
    }

    @Override
    public void upgradeUser(String email) {
        log.info("Upgrading user to premium: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPremium(true);
        userRepository.save(user);
        log.info("User {} upgraded to premium successfully", email);

        // 🚀 Send Notification via RabbitMQ
        try {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("Premium Upgrade Successful");
            notificationRequest.setMessage(
                    "Congratulations " + user.getName() + "! You are now a Premium user. Enjoy unlimited features!"
            );
            notificationRequest.setType("SUCCESS");
            notificationRequest.setCategory("SYSTEM");
            notificationRequest.setIsRead(false);

            NotificationEvent event = new NotificationEvent(
                    user.getId(),
                    user.getEmail(),
                    notificationRequest
            );

            log.info("Publishing PremiumUpgradedEvent to RabbitMQ for: {}", user.getEmail());
            rabbitTemplate.convertAndSend(
                    com.spendsmart.auth.config.RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    com.spendsmart.auth.config.RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    event
            );
            log.info("Premium upgrade notification event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish premium upgrade notification event", e);
        }
    }
}
