package com.spendsmart.notification.consumer;

import com.spendsmart.notification.config.RabbitMQConfig;
import com.spendsmart.notification.dto.NotificationEvent;
import com.spendsmart.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationService notificationService;

    /**
     * Listen for messages from the notification queue.
     * When a message arrives, this method is automatically invoked.
     */
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void consumeNotificationEvent(NotificationEvent event) {
        log.info("Event received from RabbitMQ: {}", event);

        try {
            // Process the notification using existing business logic
            notificationService.addNotification(
                    event.getUserId(),
                    event.getUserEmail(),
                    event.getRequest()
            );
            log.info("Event processed successfully for user: {}", event.getUserEmail());
        } catch (Exception e) {
            log.error("Event processing failure for user: {}. Error: {}", 
                    event.getUserEmail(), e.getMessage());
            // In a real production app, we would handle retries or dead-letter queues here.
            // For Phase 1, we keep it simple as requested.
        }
    }
}
