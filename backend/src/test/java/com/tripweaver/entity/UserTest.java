package com.tripweaver.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void shouldCreateUserWithAllFields() {
        User user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setPassword("encodedPassword");
        user.setEmail("test@example.com");
        user.setCreatedAt(LocalDateTime.now());

        assertEquals(1L, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("encodedPassword", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertNotNull(user.getCreatedAt());
    }

    @Test
    void shouldSetCreatedAtOnPrePersist() {
        User user = new User();
        user.setUsername("testuser");
        user.setPassword("password");
        user.setEmail("test@example.com");

        assertNull(user.getCreatedAt());
        user.onCreate();
        assertNotNull(user.getCreatedAt());
    }
}
