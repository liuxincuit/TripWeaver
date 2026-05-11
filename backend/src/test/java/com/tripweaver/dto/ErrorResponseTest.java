package com.tripweaver.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ErrorResponseTest {

    @Test
    void shouldCreateErrorResponse() {
        ErrorResponse response = new ErrorResponse("USER_EXISTS", "用户名已存在");

        assertEquals("USER_EXISTS", response.code());
        assertEquals("用户名已存在", response.message());
    }
}