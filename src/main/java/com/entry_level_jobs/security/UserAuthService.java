package com.entry_level_jobs.security;

import com.entry_level_jobs.model.UserAccount;
import com.entry_level_jobs.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAuthService {
    private static final List<String> USER_ROLES = List.of("ROLE_USER");

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    @Transactional
    public JwtTokenService.TokenDetails registerUser(String email, String rawPassword) {
        String normalizedEmail = normalize(email);
        userAccountRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            throw new UserAuthException(HttpStatus.CONFLICT, "Email already registered");
        });

        UserAccount user = UserAccount.builder()
                .email(normalizedEmail)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .build();
        userAccountRepository.save(user);
        log.info("Created new user with email {}", normalizedEmail);
        return jwtTokenService.generateToken(normalizedEmail, USER_ROLES);
    }

    public JwtTokenService.TokenDetails loginUser(String email, String rawPassword) {
        String normalizedEmail = normalize(email);
        UserAccount user = userAccountRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UserAuthException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new UserAuthException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.info("User {} logged in", normalizedEmail);
        return jwtTokenService.generateToken(normalizedEmail, USER_ROLES);
    }

    private String normalize(String email) {
        if (email == null) {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        return email.trim().toLowerCase();
    }
}
