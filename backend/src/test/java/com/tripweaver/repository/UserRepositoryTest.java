package com.tripweaver.repository;

import com.tripweaver.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
    }

    @Test
    void shouldSaveUser() {
        User saved = userRepository.save(testUser);

        assertNotNull(saved.getId());
        assertEquals("testuser", saved.getUsername());
    }

    @Test
    void shouldFindByUsername() {
        userRepository.save(testUser);

        Optional<User> found = userRepository.findByUsername("testuser");

        assertTrue(found.isPresent());
        assertEquals("testuser", found.get().getUsername());
    }

    @Test
    void shouldFindByEmail() {
        userRepository.save(testUser);

        Optional<User> found = userRepository.findByEmail("test@example.com");

        assertTrue(found.isPresent());
        assertEquals("test@example.com", found.get().getEmail());
    }

    @Test
    void shouldCheckExistsByUsername() {
        userRepository.save(testUser);

        assertTrue(userRepository.existsByUsername("testuser"));
        assertFalse(userRepository.existsByUsername("nonexistent"));
    }

    @Test
    void shouldCheckExistsByEmail() {
        userRepository.save(testUser);

        assertTrue(userRepository.existsByEmail("test@example.com"));
        assertFalse(userRepository.existsByEmail("nonexistent@example.com"));
    }

    @Test
    void shouldEnforceUniqueUsername() {
        userRepository.save(testUser);

        User duplicate = new User();
        duplicate.setUsername("testuser");
        duplicate.setPassword("password");
        duplicate.setEmail("another@example.com");

        assertThrows(Exception.class, () -> userRepository.saveAndFlush(duplicate));
    }

    @Test
    void shouldEnforceUniqueEmail() {
        userRepository.save(testUser);

        User duplicate = new User();
        duplicate.setUsername("another");
        duplicate.setPassword("password");
        duplicate.setEmail("test@example.com");

        assertThrows(Exception.class, () -> userRepository.saveAndFlush(duplicate));
    }
}