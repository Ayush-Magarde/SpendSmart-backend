package com.spendsmart.notification.service;

import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.entity.Notification;
import com.spendsmart.notification.exception.NotificationException;
import com.spendsmart.notification.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void addNotification_SavesAndReturnsNotification() {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("Alert");
        request.setMessage("Test message");
        request.setType("INFO");
        request.setCategory("SYSTEM");

        Notification mockSaved = new Notification();
        mockSaved.setId(1L);
        mockSaved.setUserId(1L);
        mockSaved.setTitle("Alert");
        mockSaved.setIsRead(false);

        when(notificationRepository.save(any(Notification.class))).thenReturn(mockSaved);

        Notification result = notificationService.addNotification(1L, "test@test.com", request);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUserId()).isEqualTo(1L);
        verify(notificationRepository).save(any(Notification.class));
    }

    @Test
    void getNotifications_ReturnsList() {
        Notification notif = new Notification();
        notif.setId(1L);
        when(notificationRepository.findByUserId(1L)).thenReturn(List.of(notif));

        List<Notification> result = notificationService.getNotifications(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(1L);
    }

    @Test
    void markAsRead_UpdatesNotification() {
        Notification notif = new Notification();
        notif.setId(1L);
        notif.setUserId(2L);
        notif.setIsRead(false);

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notif));

        notificationService.markAsRead(1L, 2L);

        assertThat(notif.getIsRead()).isTrue();
        verify(notificationRepository).save(notif);
    }

    @Test
    void markAsRead_ThrowsExceptionWhenNotFound() {
        when(notificationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 2L))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("Notification not found");
    }

    @Test
    void markAsRead_ThrowsExceptionWhenUnauthorized() {
        Notification notif = new Notification();
        notif.setId(1L);
        notif.setUserId(3L); // Different user

        when(notificationRepository.findById(1L)).thenReturn(Optional.of(notif));

        assertThatThrownBy(() -> notificationService.markAsRead(1L, 2L))
                .isInstanceOf(NotificationException.class)
                .hasMessageContaining("Unauthorized access");
    }
}
