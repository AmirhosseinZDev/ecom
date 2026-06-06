package com.ecommerce.application.service.jwt;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class JwtService_generateTokenUTest {

    private static final String SECRET_KEY =
            "Y2hhbmdlLW1lLWNoYW5nZS1tZS1jaGFuZ2UtbWUtY2hhbmdlLW1lLWNoYW5nZS1tZS1jaGFuZ2UtbWU=";

    @Test
    void generated_token_contains_username_and_is_valid() {
        JwtService jwtService = new JwtService(SECRET_KEY, Duration.ofHours(1));
        UserDetails userDetails = new User("0021111112", "password", List.of());

        String token = jwtService.generateToken(userDetails);

        assertEquals("0021111112", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }
}
