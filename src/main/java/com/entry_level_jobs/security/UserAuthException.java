package com.entry_level_jobs.security;

import org.springframework.http.HttpStatus;

/**
 * Runtime exception used to communicate auth/business validation problems back
 * to REST controllers with the appropriate HTTP status.
 */
public class UserAuthException extends RuntimeException {
    private final HttpStatus status;

    public UserAuthException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
