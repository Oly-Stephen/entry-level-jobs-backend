package com.entry_level_jobs.security;

import com.entry_level_jobs.config.SecurityProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminAuthServiceTest {

    private AdminAuthService adminAuthService;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = new SecurityProperties();
        properties.getAdmin().setUsername("admin");
        properties.getAdmin().setPassword("secret");
        adminAuthService = new AdminAuthService(properties);
    }

    @Test
    void authenticateReturnsTrueForValidCredentials() {
        assertTrue(adminAuthService.authenticate("admin", "secret"));
    }

    @Test
    void authenticateReturnsFalseForInvalidCredentials() {
        assertFalse(adminAuthService.authenticate("admin", "wrong"));
        assertFalse(adminAuthService.authenticate("wrong", "secret"));
    }
}
