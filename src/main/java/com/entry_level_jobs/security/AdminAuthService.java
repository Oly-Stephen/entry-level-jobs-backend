package com.entry_level_jobs.security;

import com.entry_level_jobs.config.SecurityProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * Simple admin credential validator backed by security.admin.* properties.
 */
@Service
public class AdminAuthService {
    private final SecurityProperties securityProperties;

    public AdminAuthService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    public boolean authenticate(String email, String password) {
        SecurityProperties.AdminProperties admin = securityProperties.getAdmin();
        if (admin == null || !StringUtils.hasText(admin.getEmail()) || !StringUtils.hasText(admin.getPassword())) {
            return false;
        }
        String expectedEmail = admin.getEmail().trim().toLowerCase();
        String providedEmail = email == null ? null : email.trim().toLowerCase();
        return constantTimeEquals(expectedEmail, providedEmail)
                && constantTimeEquals(admin.getPassword(), password);
    }

    private boolean constantTimeEquals(String expected, String provided) {
        if (expected == null || provided == null) {
            return false;
        }
        if (expected.length() != provided.length()) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ provided.charAt(i);
        }
        return result == 0;
    }
}
