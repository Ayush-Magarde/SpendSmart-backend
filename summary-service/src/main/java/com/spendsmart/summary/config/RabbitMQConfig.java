package com.spendsmart.summary.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String FINANCIAL_UPDATE_QUEUE = "financial.update.queue";
    public static final String FINANCIAL_UPDATE_EXCHANGE = "financial.update.exchange";
    public static final String FINANCIAL_UPDATE_ROUTING_KEY = "financial.update.key";

    @Bean
    public Queue financialUpdateQueue() {
        return new Queue(FINANCIAL_UPDATE_QUEUE, true);
    }

    @Bean
    public DirectExchange financialUpdateExchange() {
        return new DirectExchange(FINANCIAL_UPDATE_EXCHANGE);
    }

    @Bean
    public Binding financialUpdateBinding(Queue financialUpdateQueue, DirectExchange financialUpdateExchange) {
        return BindingBuilder
                .bind(financialUpdateQueue)
                .to(financialUpdateExchange)
                .with(FINANCIAL_UPDATE_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter();
        org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper typeMapper = 
            new org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper();
        
        typeMapper.setTrustedPackages("*");
        
        // Map the incoming external DTOs to the local summary DTO to prevent ClassNotFoundExceptions
        java.util.Map<String, Class<?>> idClassMapping = new java.util.HashMap<>();
        idClassMapping.put("com.spendsmart.expense.dto.FinancialUpdateEvent", com.spendsmart.summary.dto.FinancialUpdateEvent.class);
        idClassMapping.put("com.spendsmart.income.dto.FinancialUpdateEvent", com.spendsmart.summary.dto.FinancialUpdateEvent.class);
        typeMapper.setIdClassMapping(idClassMapping);
        
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
