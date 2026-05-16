package com.spendsmart.notification.controller;

import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notification Management", description = "APIs for managing notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "Create a new notification")
    public Notification createNotification(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody NotificationRequest request) {

        log.info("Creating notification for user: {}", email);
        return notificationService.addNotification(userId, email, request);
    }

    @GetMapping
    @Operation(summary = "Get all notifications for the current user")
    public List<Notification> getNotifications(
            @RequestHeader("X-User-Id") Long userId) {
        return notificationService.getNotifications(userId);
    }

    @PatchMapping("/{id}/read")
    @Operation(summary = "Mark a notification as read")
    public void markAsRead(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        notificationService.markAsRead(id, userId);
    }
}
