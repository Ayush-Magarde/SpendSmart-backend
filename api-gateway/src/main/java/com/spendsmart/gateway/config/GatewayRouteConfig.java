package com.spendsmart.gateway.config;

import com.spendsmart.gateway.filter.JwtValidationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GatewayRouteConfig {

    // ✅ Injected by Spring (IMPORTANT FIX)
    private final JwtValidationFilter jwtFilter;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()

            // Auth Service Routes (no JWT filter - for login/register)
            .route("auth-service", r -> r.path("/api/auth/**")
                .uri("lb://auth-service"))

            // OAuth2 Routes (Forwarding OAuth2 initiation and callbacks)
            .route("auth-oauth", r -> r.path("/oauth2/**", "/login/oauth2/**")
                .uri("lb://auth-service"))

            // User Profile Routes (with JWT filter)
            .route("user-service", r -> r.path("/api/user/**")
                .filters(f -> f.filter(jwtFilter))
                .uri("lb://auth-service"))

            // Expense Service
            .route("expense-service", r -> r.path("/api/expenses/**")
                .filters(f -> f.filter(jwtFilter))
                .uri("lb://expense-service"))

            // Income Service
            .route("income-service", r -> r.path("/api/incomes/**")
                .filters(f -> f.filter(jwtFilter))
                .uri("lb://income-service"))

            // Category Service
            .route("category-service", r -> r.path("/api/categories/**")
                .filters(f -> f.filter(jwtFilter))
                .uri("lb://category-service"))

            // Summary Service
            .route("summary-service", r -> r.path("/api/summary/**")
                .filters(f -> f.filter(jwtFilter))
                .uri("lb://summary-service"))

            // Budget Service
            .route("budget-service", r -> r.path("/api/budgets/**")
                .filters(f -> f.filter(jwtFilter))
                .uri("lb://budget-service"))

            // Payment Service
            .route("payment-service", r -> r.path("/api/payments/**")
                .filters(f -> f.filter(jwtFilter))
                .uri("lb://payment-service"))

            // Notification Service
            .route("notification-service", r -> r.path("/api/notifications/**")
                .filters(f -> f.filter(jwtFilter))
                .uri("lb://notification-service"))

            // Recurring Service
            .route("recurring-service", r -> r.path("/api/recurring/**")
                .filters(f -> f.filter(jwtFilter))
                .uri("lb://recurring-service"))

            .build();
    }
}