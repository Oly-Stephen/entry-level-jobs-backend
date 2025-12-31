package com.entry_level_jobs.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads JWT tokens from the Authorization header and populates the security
 * context.
 */
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTH_SCHEME = "Bearer ";
    private static final List<String> PUBLIC_MATCHERS = List.of(
            "/api/admin/token");

    private final JwtTokenService jwtTokenService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith(AUTH_SCHEME)) {
            String token = authHeader.substring(AUTH_SCHEME.length());
            jwtTokenService.parseToken(token).ifPresent(claims -> setAuthentication(claims, request, token));
        }
        filterChain.doFilter(request, response);
    }

    private void setAuthentication(Jws<Claims> claims, HttpServletRequest request, String rawToken) {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            return;
        }
        String subject = claims.getPayload().getSubject();
        Collection<? extends GrantedAuthority> authorities = buildAuthorities(claims.getPayload());
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(subject, rawToken,
                authorities);
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(Claims claims) {
        List<String> roles = jwtTokenService.extractRoles(claims);
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toUnmodifiableList());
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return PUBLIC_MATCHERS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
