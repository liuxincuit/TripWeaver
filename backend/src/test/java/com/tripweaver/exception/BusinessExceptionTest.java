package com.tripweaver.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.*;

class BusinessExceptionTest {

    @Test
    void shouldCreateExceptionWithAllFields() {
        BusinessException exception = new BusinessException(
            "用户名已存在",
            "USER_EXISTS",
            HttpStatus.BAD_REQUEST
        );

        assertEquals("用户名已存在", exception.getMessage());
        assertEquals("USER_EXISTS", exception.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCode() {
        BusinessException exception = new BusinessException(
            "计划不存在",
            "PLAN_NOT_FOUND"
        );

        assertEquals("计划不存在", exception.getMessage());
        assertEquals("PLAN_NOT_FOUND", exception.getErrorCode());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}