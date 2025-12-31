package com.entry_level_jobs.security;

import com.entry_level_jobs.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;

    @BeforeEach
    void setUp() {
        SecurityProperties properties = new SecurityProperties();
        properties.getJwt().setSecret("test-secret-test-secret-test-secret-123456");
        properties.getJwt().setExpirationSeconds(120);
        jwtTokenService = new JwtTokenService(properties);
        jwtTokenService.init();
    }

    @Test
    void generateTokenContainsSubjectAndRole() {
        JwtTokenService.TokenDetails tokenDetails = jwtTokenService.generateAdminToken("admin");
        assertTrue(tokenDetails.expiresAt().isAfter(Instant.now()));

        Jws<Claims> claims = jwtTokenService.parseToken(tokenDetails.token()).orElseThrow();
        assertEquals("admin", claims.getPayload().getSubject());
        assertTrue(jwtTokenService.extractRoles(claims.getPayload()).contains("ROLE_ADMIN"));
    }
}
