package com.tripweaver.exception;

import com.tripweaver.dto.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void shouldHandleBusinessException() {
        BusinessException exception = new BusinessException(
            "用户名已存在",
            "USER_EXISTS",
            HttpStatus.BAD_REQUEST
        );

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("USER_EXISTS", response.getBody().code());
        assertEquals("用户名已存在", response.getBody().message());
    }

    @Test
    void shouldHandleBusinessExceptionWithDefaultStatus() {
        BusinessException exception = new BusinessException("计划不存在", "PLAN_NOT_FOUND");

        ResponseEntity<ErrorResponse> response = handler.handleBusinessException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("PLAN_NOT_FOUND", response.getBody().code());
        assertEquals("计划不存在", response.getBody().message());
    }

    @Test
    void shouldHandleValidationException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "username", "不能为空");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));

        ResponseEntity<ErrorResponse> response = handler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("VALIDATION_ERROR", response.getBody().code());
        assertTrue(response.getBody().message().contains("username"));
    }

    @Test
    void shouldHandleAuthenticationException() {
        BadCredentialsException exception = new BadCredentialsException("用户名或密码错误");

        ResponseEntity<ErrorResponse> response = handler.handleAuthenticationException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("AUTHENTICATION_FAILED", response.getBody().code());
        assertEquals("用户名或密码错误", response.getBody().message());
    }

    @Test
    void shouldHandleAccessDeniedException() {
        AccessDeniedException exception = new AccessDeniedException("权限不足");

        ResponseEntity<ErrorResponse> response = handler.handleAccessDeniedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ACCESS_DENIED", response.getBody().code());
        assertEquals("权限不足", response.getBody().message());
    }

    @Test
    void shouldHandleGenericException() {
        RuntimeException exception = new RuntimeException("Unexpected error");

        ResponseEntity<ErrorResponse> response = handler.handleGenericException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertEquals("服务器内部错误", response.getBody().message());
    }
}