package com.entry_level_jobs.model;

public enum UserRole {
    USER,
    ADMIN;

    public String asAuthority() {
        return "ROLE_" + name();
    }
}
