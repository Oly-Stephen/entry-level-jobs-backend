package com.entry_level_jobs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic paginated response wrapper.
 * Wraps list of data with pagination metadata.
 * 
 * @param <T> Type of data being paginated
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {
    /**
     * Indicates if the request was successful
     */
    @JsonProperty("success")
    private boolean success;

    /**
     * HTTP status message
     */
    @JsonProperty("message")
    private String message;

    /**
     * List of data items for current page
     */
    @JsonProperty("data")
    private List<T> data;

    /**
     * Pagination metadata
     */
    @JsonProperty("pagination")
    private PaginationDTO pagination;

    /**
     * Error message (if any)
     */
    @JsonProperty("error")
    private String error;

    /**
     * Static factory method to create successful paginated response
     */
    public static <T> PaginatedResponse<T> success(List<T> data, PaginationDTO pagination) {
        return PaginatedResponse.<T>builder()
                .success(true)
                .message("Data retrieved successfully")
                .data(data)
                .pagination(pagination)
                .build();
    }

    /**
     * Static factory method to create error response
     */
    public static <T> PaginatedResponse<T> error(String message, String errorDetails) {
        return PaginatedResponse.<T>builder()
                .success(false)
                .message(message)
                .error(errorDetails)
                .build();
    }
}
