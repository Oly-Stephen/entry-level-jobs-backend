package com.entry_level_jobs.controller;

import com.entry_level_jobs.dto.SignupRequest;
import com.entry_level_jobs.dto.TokenResponse;
import com.entry_level_jobs.dto.UserLoginRequest;
import com.entry_level_jobs.security.UserAuthException;
import com.entry_level_jobs.security.UserAuthService;
import com.entry_level_jobs.security.JwtTokenService.TokenDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class UserAuthController {

    private final UserAuthService userAuthService;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            TokenDetails token = userAuthService.registerUser(request.getEmail(), request.getPassword());
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new TokenResponse(token.token(), token.expiresAt()));
        } catch (UserAuthException ex) {
            log.warn("Signup failed for {} - {}", request.getEmail(), ex.getMessage());
            return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody UserLoginRequest request) {
        try {
            TokenDetails token = userAuthService.loginUser(request.getEmail(), request.getPassword());
            return ResponseEntity.ok(new TokenResponse(token.token(), token.expiresAt()));
        } catch (UserAuthException ex) {
            log.warn("Login failed for {} - {}", request.getEmail(), ex.getMessage());
            return ResponseEntity.status(ex.getStatus()).body(Map.of("error", ex.getMessage()));
        }
    }
}
