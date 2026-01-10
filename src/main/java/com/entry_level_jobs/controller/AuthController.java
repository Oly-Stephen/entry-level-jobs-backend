package com.entry_level_jobs.controller;

import com.entry_level_jobs.dto.auth.AuthResponse;
import com.entry_level_jobs.dto.auth.LoginRequest;
import com.entry_level_jobs.dto.auth.RegisterRequest;
import com.entry_level_jobs.security.UserAuthException;
import com.entry_level_jobs.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (UserAuthException ex) {
            log.warn("Registration failed for {} - {}", request.getEmail(), ex.getMessage());
            return ResponseEntity.status(ex.getStatus()).body(java.util.Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (UserAuthException ex) {
            log.warn("Login failed for {} - {}", request.getEmail(), ex.getMessage());
            return ResponseEntity.status(ex.getStatus()).body(java.util.Map.of("error", ex.getMessage()));
        }
    }
}
