package com.entry_level_jobs.security;

import com.entry_level_jobs.model.UserAccount;
import com.entry_level_jobs.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserAuthServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenService jwtTokenService;

    private UserAuthService userAuthService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userAuthService = new UserAuthService(userAccountRepository, passwordEncoder, jwtTokenService);
    }

    @Test
    void registerUserCreatesNewAccount() {
        when(userAccountRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed");
        JwtTokenService.TokenDetails tokenDetails = new JwtTokenService.TokenDetails("token", Instant.now());
        when(jwtTokenService.generateToken(eq("user@example.com"), anyList())).thenReturn(tokenDetails);
        when(userAccountRepository.save(any(UserAccount.class))).thenAnswer(inv -> inv.getArgument(0));

        JwtTokenService.TokenDetails result = userAuthService.registerUser("user@example.com", "password123");

        assertEquals(tokenDetails, result);
        ArgumentCaptor<UserAccount> captor = ArgumentCaptor.forClass(UserAccount.class);
        verify(userAccountRepository).save(captor.capture());
        assertEquals("user@example.com", captor.getValue().getEmail());
        assertEquals("hashed", captor.getValue().getPasswordHash());
    }

    @Test
    void registerUserThrowsWhenEmailExists() {
        when(userAccountRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(new UserAccount()));

        UserAuthException ex = assertThrows(UserAuthException.class,
                () -> userAuthService.registerUser("user@example.com", "password123"));
        assertEquals(HttpStatus.CONFLICT, ex.getStatus());
        verify(userAccountRepository, never()).save(any());
    }

    @Test
    void loginUserReturnsTokenWhenPasswordMatches() {
        UserAccount user = UserAccount.builder()
                .email("user@example.com")
                .passwordHash("hashed")
                .build();
        when(userAccountRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password123", "hashed")).thenReturn(true);
        JwtTokenService.TokenDetails tokenDetails = new JwtTokenService.TokenDetails("token", Instant.now());
        when(jwtTokenService.generateToken(eq("user@example.com"), anyList())).thenReturn(tokenDetails);

        JwtTokenService.TokenDetails result = userAuthService.loginUser("user@example.com", "password123");

        assertEquals(tokenDetails, result);
    }

    @Test
    void loginUserThrowsOnInvalidPassword() {
        UserAccount user = UserAccount.builder()
                .email("user@example.com")
                .passwordHash("hashed")
                .build();
        when(userAccountRepository.findByEmailIgnoreCase("user@example.com"))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        UserAuthException ex = assertThrows(UserAuthException.class,
                () -> userAuthService.loginUser("user@example.com", "wrong"));
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getStatus());
    }
}
