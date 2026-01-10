package com.entry_level_jobs.dto.auth;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class AuthResponse {
    String token;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    Instant expiresAt;

    UserProfile profile;
}
