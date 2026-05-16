package com.spendsmart.auth.config;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * EXCHANGE: The routing engine. 
     * Even as a producer, it's good practice to know where we are sending.
     */
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    /**
     * ROUTING KEY: The address we attach to our message so the 
     * exchange knows which queue to deliver it to.
     */
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    @Bean
    public org.springframework.amqp.core.Queue notificationQueue() {
        return new org.springframework.amqp.core.Queue(NOTIFICATION_QUEUE, true);
    }

    @Bean
    public org.springframework.amqp.core.DirectExchange notificationExchange() {
        return new org.springframework.amqp.core.DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public org.springframework.amqp.core.Binding notificationBinding(
            org.springframework.amqp.core.Queue notificationQueue, 
            org.springframework.amqp.core.DirectExchange notificationExchange) {
        return org.springframework.amqp.core.BindingBuilder
                .bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * JSON CONVERTER: Converts Java objects into JSON format for 
     * compatibility across different services.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate amqpTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
