package com.spendsmart.auth.controller;

import com.spendsmart.auth.dto.AuthResponse;
import com.spendsmart.auth.entity.User;
import com.spendsmart.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserRepository userRepository;
    private final com.spendsmart.auth.service.AuthService authService;

    @GetMapping("/profile")
    public AuthResponse getProfile(@RequestHeader("X-User-Email") String email) {
        log.info("Fetching profile for user: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return new AuthResponse(
                null, // token not needed for profile fetch
                user.getEmail(),
                user.getName(),
                user.isPremium(),
                user.getRole()
        );
    }

    @PostMapping("/internal/upgrade")
    public void upgradeUser(@RequestParam String email) {
        log.info("Internal request to upgrade user: {}", email);
        authService.upgradeUser(email);
    }
    @GetMapping("/admin/users/count")
    public long getUsersCount(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) {
            throw new SecurityException("Unauthorized");
        }
        log.info("Admin request: total users count");
        return userRepository.countByRole("USER");
    }

    @GetMapping("/admin/users/premium/count")
    public long getPremiumUsersCount(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) {
            throw new SecurityException("Unauthorized");
        }
        log.info("Admin request: premium users count");
        return userRepository.countByIsPremiumTrue();
    }
}
