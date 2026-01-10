package com.entry_level_jobs.dto.auth;

import com.entry_level_jobs.model.UserRole;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class UserProfile {
    @JsonProperty("id")
    Long userId;

    String email;

    @JsonProperty("display_name")
    String displayName;

    UserRole role;

    @JsonProperty("email_verified")
    boolean emailVerified;
}
