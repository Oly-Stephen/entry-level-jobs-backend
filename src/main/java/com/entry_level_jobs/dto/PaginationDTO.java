package com.entry_level_jobs.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for pagination metadata.
 * Contains pagination information such as current page, size, total elements,
 * etc.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDTO {
    /**
     * Current page number (0-indexed)
     */
    private int currentPage;

    /**
     * Number of items per page
     */
    private int pageSize;

    /**
     * Total number of elements across all pages
     */
    private long totalElements;

    /**
     * Total number of pages
     */
    private int totalPages;

    /**
     * Whether this is the first page
     */
    private boolean isFirstPage;

    /**
     * Whether this is the last page
     */
    private boolean isLastPage;

    /**
     * Whether there is a next page
     */
    private boolean hasNextPage;

    /**
     * Whether there is a previous page
     */
    private boolean hasPreviousPage;

    /**
     * Number of items in current page
     */
    private int numberOfElements;
}
