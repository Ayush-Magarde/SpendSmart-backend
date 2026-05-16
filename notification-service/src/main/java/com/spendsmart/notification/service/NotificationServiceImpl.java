package com.spendsmart.notification.service;

import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;

    @Override
    @Transactional
    public Notification addNotification(Long userId, String email, NotificationRequest request) {
        log.info("Adding notification for user: {}", email);

        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(request.getTitle());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setCategory(request.getCategory());
        notification.setCreatedAt(java.time.LocalDateTime.now());
        notification.setIsRead(request.getIsRead() != null ? request.getIsRead() : false);

        Notification savedNotification = notificationRepository.save(notification);

        return savedNotification;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getNotifications(Long userId) {
        log.info("Retrieving notifications for user ID: {}", userId);
        return notificationRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long id, Long userId) {
        log.info("Marking notification {} as read for user ID: {}", id, userId);
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new com.spendsmart.notification.exception.NotificationException("Notification not found"));
        
        if (!notification.getUserId().equals(userId)) {
            throw new com.spendsmart.notification.exception.NotificationException("Unauthorized access to notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}
