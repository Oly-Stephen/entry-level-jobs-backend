package com.entry_level_jobs.security;

import com.entry_level_jobs.config.SecurityProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.WeakKeyException;
import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKey;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Handles JWT creation and validation for admin access.
 */
@Slf4j
@Service
public class JwtTokenService {
    private static final String ROLES_CLAIM = "roles";

    private final SecurityProperties securityProperties;
    private SecretKey signingKey;

    @Getter
    private long expirationSeconds;

    public JwtTokenService(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
    }

    @PostConstruct
    void init() {
        String secret = securityProperties.getJwt().getSecret();
        if (!StringUtils.hasText(secret)) {
            throw new IllegalStateException("security.jwt.secret must be configured");
        }
        try {
            this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        } catch (WeakKeyException e) {
            throw new IllegalStateException("security.jwt.secret must be at least 256 bits", e);
        }
        this.expirationSeconds = Math.max(securityProperties.getJwt().getExpirationSeconds(), 60);
    }

    public TokenDetails generateAdminToken(String subject) {
        return generateToken(subject, List.of("ROLE_ADMIN"));
    }

    public TokenDetails generateToken(String subject, List<String> roles) {
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plusSeconds(expirationSeconds);

        List<String> grantedRoles = roles == null ? List.of() : List.copyOf(roles);

        String token = Jwts.builder()
                .subject(subject)
                .claim(ROLES_CLAIM, grantedRoles)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiresAt))
                .signWith(signingKey)
                .compact();

        return new TokenDetails(token, expiresAt);
    }

    public Optional<Jws<Claims>> parseToken(String token) {
        if (!StringUtils.hasText(token)) {
            return Optional.empty();
        }
        try {
            Jws<Claims> claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token);
            return Optional.of(claims);
        } catch (Exception ex) {
            log.debug("Failed to parse JWT", ex);
            return Optional.empty();
        }
    }

    public record TokenDetails(String token, Instant expiresAt) {
    }

    public List<String> extractRoles(Claims claims) {
        Object roles = claims.get(ROLES_CLAIM);
        if (roles instanceof List<?> roleList) {
            return roleList.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
