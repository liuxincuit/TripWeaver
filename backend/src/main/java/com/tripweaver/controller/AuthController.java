package com.tripweaver.controller;

import com.tripweaver.dto.AuthResponse;
import com.tripweaver.dto.LoginRequest;
import com.tripweaver.dto.RegisterRequest;
import com.tripweaver.dto.UserResponse;
import com.tripweaver.entity.User;
import com.tripweaver.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser() {
        User user = userService.getCurrentUser();
        UserResponse response = new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail()
        );
        return ResponseEntity.ok(response);
    }
}