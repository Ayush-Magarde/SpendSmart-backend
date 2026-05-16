package com.spendsmart.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event DTO for RabbitMQ messages.
 * Contains user identification and the notification details.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent {
    private Long userId;
    private String userEmail;
    private NotificationRequest request;
}
