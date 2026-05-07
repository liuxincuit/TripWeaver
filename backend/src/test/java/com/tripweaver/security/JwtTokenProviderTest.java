package com.tripweaver.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
            "test-secret-key-must-be-at-least-32-characters-long");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpiration", 86400000L);
    }

    @Test
    void shouldGenerateToken() {
        String token = tokenProvider.generateToken("testuser");

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void shouldExtractUsernameFromToken() {
        String token = tokenProvider.generateToken("testuser");

        String username = tokenProvider.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    @Test
    void shouldValidateValidToken() {
        String token = tokenProvider.generateToken("testuser");

        assertTrue(tokenProvider.validateToken(token));
    }

    @Test
    void shouldRejectInvalidToken() {
        String invalidToken = "invalid.token.here";

        assertFalse(tokenProvider.validateToken(invalidToken));
    }

    @Test
    void shouldRejectMalformedToken() {
        String malformedToken = "not-a-jwt-token";

        assertFalse(tokenProvider.validateToken(malformedToken));
    }

    @Test
    void shouldGenerateDifferentTokensForDifferentUsers() {
        String token1 = tokenProvider.generateToken("user1");
        String token2 = tokenProvider.generateToken("user2");

        assertNotEquals(token1, token2);
    }
}