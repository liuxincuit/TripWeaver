package com.tripweaver.config;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class SearXNGPropertiesTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void shouldFailWhenBaseUrlIsBlank() {
        SearXNGProperties properties = new SearXNGProperties();
        properties.setBaseUrl("");
        properties.setRetryCount(3);
        properties.setRetryDelayMs(1000);

        Set<ConstraintViolation<SearXNGProperties>> violations = validator.validate(properties);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("SearXNG base URL is required")));
    }

    @Test
    void shouldPassWhenBaseUrlIsValid() {
        SearXNGProperties properties = new SearXNGProperties();
        properties.setBaseUrl("http://localhost:8080");
        properties.setRetryCount(3);
        properties.setRetryDelayMs(1000);

        Set<ConstraintViolation<SearXNGProperties>> violations = validator.validate(properties);
        assertTrue(violations.isEmpty());
    }
}
