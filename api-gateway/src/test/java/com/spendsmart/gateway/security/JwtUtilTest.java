package com.spendsmart.gateway.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private final JwtUtil jwtUtil = new JwtUtil();
    private final String secret = "this_is_a_very_long_secret_key_for_jwt_purposes_that_is_at_least_256_bits_long_123456789";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
    }

    private String generateToken(String email, String role) {
        Key key = Keys.hmacShaKeyFor(secret.getBytes());
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(email)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void extractEmail() {
        String token = generateToken("test@test.com", "USER");
        String email = jwtUtil.extractEmail(token);
        assertThat(email).isEqualTo("test@test.com");
    }

    @Test
    void validateToken_Valid() {
        String token = generateToken("test@test.com", "USER");
        boolean isValid = jwtUtil.validateToken(token);
        assertThat(isValid).isTrue();
    }

    @Test
    void validateToken_Invalid() {
        boolean isValid = jwtUtil.validateToken("invalid_token");
        assertThat(isValid).isFalse();
    }

    @Test
    void extractRole() {
        String token = generateToken("test@test.com", "ADMIN");
        String role = jwtUtil.extractRole(token);
        assertThat(role).isEqualTo("ADMIN");
    }
}
