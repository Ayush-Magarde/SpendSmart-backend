package com.spendsmart.notification.service;

import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import java.util.List;

public interface NotificationService {

    Notification addNotification(Long userId, String email, NotificationRequest request);
    
    List<Notification> getNotifications(Long userId);
    
    void markAsRead(Long id, Long userId);
}
