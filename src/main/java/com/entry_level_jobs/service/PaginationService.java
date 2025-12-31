package com.entry_level_jobs.service;

import com.entry_level_jobs.dto.PaginationDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

/**
 * Service for pagination operations.
 * Converts Spring Data Page objects to PaginationDTO.
 * Follows clean architecture principle of separating business logic from
 * framework.
 */
@Service
@Slf4j
public class PaginationService {

    /**
     * Convert Spring Data Page to PaginationDTO
     * 
     * @param page Spring Data Page object
     * @return PaginationDTO with pagination metadata
     */
    public <T> PaginationDTO toPaginationDTO(Page<T> page) {
        log.debug("Converting Page to PaginationDTO: pageNumber={}, pageSize={}, totalElements={}",
                page.getNumber(), page.getSize(), page.getTotalElements());

        PaginationDTO pagination = PaginationDTO.builder()
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirstPage(page.isFirst())
                .isLastPage(page.isLast())
                .hasNextPage(page.hasNext())
                .hasPreviousPage(page.hasPrevious())
                .numberOfElements(page.getNumberOfElements())
                .build();

        log.debug("PaginationDTO created: {}", pagination);
        return pagination;
    }

    /**
     * Validate pagination parameters
     * 
     * @param page page number (0-indexed)
     * @param size page size
     * @return true if valid, false otherwise
     */
    public boolean validatePaginationParams(int page, int size) {
        if (page < 0) {
            log.warn("Invalid page number: {}", page);
            return false;
        }
        if (size < 1) {
            log.warn("Invalid page size: {}", size);
            return false;
        }
        if (size > 100) {
            log.warn("Page size {} exceeds maximum of 100", size);
            return false;
        }
        return true;
    }

    /**
     * Get default page size
     * 
     * @return default page size
     */
    public int getDefaultPageSize() {
        return 10;
    }

    /**
     * Get maximum page size
     * 
     * @return maximum allowed page size
     */
    public int getMaxPageSize() {
        return 100;
    }

    /**
     * Sanitize page size (ensure it doesn't exceed maximum)
     * 
     * @param size requested page size
     * @return sanitized page size
     */
    public int sanitizePageSize(int size) {
        if (size <= 0) {
            return getDefaultPageSize();
        }
        return Math.min(size, getMaxPageSize());
    }
}
