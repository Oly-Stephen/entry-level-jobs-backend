package com.entry_level_jobs.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

@Data
@AllArgsConstructor
public class TokenResponse {
    private String token;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant expiresAt;
}
