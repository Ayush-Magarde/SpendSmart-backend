package com.spendsmart.expense.config;

import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FINANCIAL_UPDATE_EXCHANGE = "financial.update.exchange";
    public static final String FINANCIAL_UPDATE_ROUTING_KEY = "financial.update.key";
    public static final String FINANCIAL_UPDATE_QUEUE = "financial.update.queue";

    @Bean
    public org.springframework.amqp.core.Queue financialUpdateQueue() {
        return new org.springframework.amqp.core.Queue(FINANCIAL_UPDATE_QUEUE, true);
    }

    @Bean
    public DirectExchange financialUpdateExchange() {
        return new DirectExchange(FINANCIAL_UPDATE_EXCHANGE);
    }

    @Bean
    public org.springframework.amqp.core.Binding financialUpdateBinding(
            org.springframework.amqp.core.Queue financialUpdateQueue, 
            DirectExchange financialUpdateExchange) {
        return org.springframework.amqp.core.BindingBuilder
                .bind(financialUpdateQueue)
                .to(financialUpdateExchange)
                .with(FINANCIAL_UPDATE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
