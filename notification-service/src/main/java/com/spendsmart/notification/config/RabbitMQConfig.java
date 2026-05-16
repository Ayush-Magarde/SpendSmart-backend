package com.spendsmart.notification.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    /**
     * QUEUE: A buffer that stores messages until they are consumed.
     * "notification.queue" is where notification events will wait.
     */
    public static final String NOTIFICATION_QUEUE = "notification.queue";

    /**
     * EXCHANGE: The routing engine. Producers send messages to an exchange, 
     * which then routes them to queues based on routing keys.
     * We use a "Direct" exchange for simple 1-to-1 routing.
     */
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";

    /**
     * ROUTING KEY: A virtual address that the exchange uses to decide 
     * which queue the message should go to.
     */
    public static final String NOTIFICATION_ROUTING_KEY = "notification.routing.key";

    @Bean
    public Queue notificationQueue() {
        return new Queue(NOTIFICATION_QUEUE, true); // true = durable (survives broker restart)
    }

    @Bean
    public DirectExchange notificationExchange() {
        return new DirectExchange(NOTIFICATION_EXCHANGE);
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, DirectExchange notificationExchange) {
        return BindingBuilder
                .bind(notificationQueue)
                .to(notificationExchange)
                .with(NOTIFICATION_ROUTING_KEY);
    }

    /**
     * JSON CONVERTER: Ensures that Java objects are converted to JSON strings
     * when sent to RabbitMQ, and back to Java objects when received.
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
