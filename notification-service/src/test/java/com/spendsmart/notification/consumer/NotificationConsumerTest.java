package com.spendsmart.notification.consumer;

import com.spendsmart.notification.dto.NotificationEvent;
import com.spendsmart.notification.dto.NotificationRequest;
import com.spendsmart.notification.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class NotificationConsumerTest {

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private NotificationConsumer notificationConsumer;

    @Test
    void consumeNotificationEvent_Success() {
        NotificationRequest request = new NotificationRequest();
        request.setTitle("Test");
        NotificationEvent event = new NotificationEvent(1L, "test@test.com", request);

        notificationConsumer.consumeNotificationEvent(event);

        verify(notificationService).addNotification(eq(1L), eq("test@test.com"), any(NotificationRequest.class));
    }

    @Test
    void consumeNotificationEvent_Failure() {
        NotificationRequest request = new NotificationRequest();
        NotificationEvent event = new NotificationEvent(1L, "test@test.com", request);

        doThrow(new RuntimeException("Error")).when(notificationService)
                .addNotification(any(), any(), any());

        notificationConsumer.consumeNotificationEvent(event);

        verify(notificationService).addNotification(any(), any(), any());
    }
}
