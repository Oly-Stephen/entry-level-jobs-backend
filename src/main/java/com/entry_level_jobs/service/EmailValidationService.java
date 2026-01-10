package com.entry_level_jobs.service;

import com.entry_level_jobs.security.UserAuthException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.regex.Pattern;

@Service
public class EmailValidationService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Set<String> BLOCKED_DOMAINS = Set.of("example.com", "test.com");

    public void validate(String email) {
        if (email == null || email.isBlank()) {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Email is required");
        }
        String trimmed = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(trimmed).matches()) {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Invalid email format");
        }
        String domain = trimmed.substring(trimmed.indexOf('@') + 1);
        if (BLOCKED_DOMAINS.contains(domain)) {
            throw new UserAuthException(HttpStatus.BAD_REQUEST, "Disposable email domains are not allowed");
        }
    }
}
