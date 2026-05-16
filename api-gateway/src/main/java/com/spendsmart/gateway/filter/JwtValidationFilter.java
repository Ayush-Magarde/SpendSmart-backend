package com.spendsmart.gateway.filter;

import com.spendsmart.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class JwtValidationFilter implements GatewayFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;
    
    // Skip authentication for these paths
    private static final List<String> EXCLUDED_PATHS = List.of(
        "/api/auth/login",
        "/api/auth/register",
        "/api/auth/register/init",
        "/api/auth/register/verify",
        "/actuator/health",
        "/actuator/info"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 1. BLOCK EXTERNAL ACCESS TO INTERNAL ENDPOINTS
        if (path.contains("/internal/")) {
            log.warn("Unauthorized attempt to access internal endpoint: {}", path);
            return handleForbidden(exchange, "Access to internal endpoints is restricted");
        }

        // 2. Skip authentication for excluded paths
        if (isExcludedPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = request.getHeaders().getFirst("Authorization");
        
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return handleUnauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(7);
        
        try {
            Claims claims = jwtUtil.extractAllClaims(token);
            
            // Extract user info and add to headers for downstream services
            String userEmail = claims.getSubject();
            String userId = claims.getId();
            String userRole = claims.get("role", String.class);
            
            log.info("Extracted userId from JWT: {}", userId);
            
            ServerHttpRequest modifiedRequest = request.mutate()
                .header("X-User-Email", userEmail)
                .header("X-User-Id", userId != null ? userId : "")
                .header("X-User-Role", userRole != null ? userRole : "")
                .build();
            
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
            
        } catch (Exception e) {
            return handleUnauthorized(exchange, "Invalid or expired JWT token");
        }
    }

    private boolean isExcludedPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(excludedPath -> 
            path.equals(excludedPath) || path.startsWith(excludedPath)
        );
    }

    private Mono<Void> handleForbidden(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.FORBIDDEN);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format(
            "{\"timestamp\":\"%s\",\"status\":403,\"error\":\"Forbidden\",\"message\":\"%s\",\"path\":\"%s\"}",
            java.time.LocalDateTime.now(),
            message,
            exchange.getRequest().getURI().getPath()
        );
        
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }

    private Mono<Void> handleUnauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add("Content-Type", "application/json");
        
        String body = String.format(
            "{\"timestamp\":\"%s\",\"status\":401,\"error\":\"Unauthorized\",\"message\":\"%s\",\"path\":\"%s\"}",
            java.time.LocalDateTime.now(),
            message,
            exchange.getRequest().getURI().getPath()
        );
        
        return response.writeWith(
            Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8)))
        );
    }

    @Override
    public int getOrder() {
        return -100; // High priority to run early
    }
}
