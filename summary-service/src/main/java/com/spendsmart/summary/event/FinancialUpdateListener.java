package com.spendsmart.summary.event;

import com.spendsmart.summary.config.RabbitMQConfig;
import com.spendsmart.summary.dto.FinancialUpdateEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FinancialUpdateListener {

    @RabbitListener(queues = RabbitMQConfig.FINANCIAL_UPDATE_QUEUE)
    @CacheEvict(value = "dashboard_summary", key = "#event.userId")
    public void handleFinancialUpdate(FinancialUpdateEvent event) {
        log.info("Received financial update for user: {}. Evicting dashboard summary cache.", event.getUserId());
    }
}
