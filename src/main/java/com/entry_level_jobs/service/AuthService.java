package com.entry_level_jobs.service;

import com.entry_level_jobs.dto.auth.AuthResponse;
import com.entry_level_jobs.dto.auth.LoginRequest;
import com.entry_level_jobs.dto.auth.RegisterRequest;
import com.entry_level_jobs.dto.auth.UserProfile;
import com.entry_level_jobs.model.UserAccount;
import com.entry_level_jobs.model.UserRole;
import com.entry_level_jobs.repository.UserAccountRepository;
import com.entry_level_jobs.security.UserAuthException;
import com.entry_level_jobs.security.JwtTokenService;
import com.entry_level_jobs.security.JwtTokenService.TokenDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;
    private final EmailValidationService emailValidationService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        emailValidationService.validate(request.getEmail());
        userAccountRepository.findByEmailIgnoreCase(request.getEmail().trim())
                .ifPresent(existing -> {
                    throw new UserAuthException(HttpStatus.CONFLICT, "Email already registered");
                });

        UserAccount account = UserAccount.builder()
                .email(request.getEmail().trim().toLowerCase())
                .displayName(request.getDisplayName())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.USER)
                .emailVerified(false)
                .active(true)
                .build();
        userAccountRepository.save(account);
        log.info("Registered new user {}", account.getEmail());
        return buildAuthResponse(account);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        emailValidationService.validate(request.getEmail());
        UserAccount account = userAccountRepository.findByEmailIgnoreCaseAndActiveTrue(request.getEmail().trim())
                .orElseThrow(() -> new UserAuthException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(request.getPassword(), account.getPasswordHash())) {
            throw new UserAuthException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        account.setLastLoginAt(LocalDateTime.now());
        userAccountRepository.save(account);
        log.info("{} authenticated", account.getEmail());
        return buildAuthResponse(account);
    }

    private AuthResponse buildAuthResponse(UserAccount account) {
        TokenDetails token = jwtTokenService.generateToken(account.getEmail(),
                List.of(account.getRole().asAuthority()));
        UserProfile profile = UserProfile.builder()
                .userId(account.getId())
                .email(account.getEmail())
                .displayName(account.getDisplayName())
                .role(account.getRole())
                .emailVerified(account.isEmailVerified())
                .build();
        return AuthResponse.builder()
                .token(token.token())
                .expiresAt(token.expiresAt())
                .profile(profile)
                .build();
    }
}
