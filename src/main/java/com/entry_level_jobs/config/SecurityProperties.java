package com.entry_level_jobs.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Application security properties bound from application.properties (or env
 * overrides).
 */
@Data
@Component
@ConfigurationProperties(prefix = "security")
public class SecurityProperties {
    private AdminProperties admin = new AdminProperties();
    private JwtProperties jwt = new JwtProperties();

    @Data
    public static class AdminProperties {
        private String username;
        private String password;
    }

    @Data
    public static class JwtProperties {
        private String secret;
        private long expirationSeconds = 3600;
    }
}
