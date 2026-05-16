package com.spendsmart.recurring.client;

import com.spendsmart.recurring.dto.NotificationRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "notification-service")
public interface NotificationServiceClient {
    
    @PostMapping("/api/notifications")
    void createNotification(
        @RequestHeader("X-User-Email") String email,
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody NotificationRequest request
    );
}
