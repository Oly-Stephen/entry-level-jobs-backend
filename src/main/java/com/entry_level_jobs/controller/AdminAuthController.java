package com.entry_level_jobs.controller;

import com.entry_level_jobs.dto.AdminLoginRequest;
import com.entry_level_jobs.dto.TokenResponse;
import com.entry_level_jobs.security.AdminAuthService;
import com.entry_level_jobs.security.JwtTokenService;
import com.entry_level_jobs.security.JwtTokenService.TokenDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> generateToken(@Valid @RequestBody AdminLoginRequest request) {
        log.info("Admin token request received for user {}", request.getUsername());
        if (!adminAuthService.authenticate(request.getUsername(), request.getPassword())) {
            log.warn("Invalid admin credentials for user {}", request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TokenDetails tokenDetails = jwtTokenService.generateAdminToken(request.getUsername());
        return ResponseEntity.ok(new TokenResponse(tokenDetails.token(), tokenDetails.expiresAt()));
    }
}
