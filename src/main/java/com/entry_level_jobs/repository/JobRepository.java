package com.entry_level_jobs.repository;

import com.entry_level_jobs.dto.LocationOption;
import com.entry_level_jobs.model.Job;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Job entity.
 * Provides database operations for jobs.
 */
@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    /**
     * Find a job by its URL
     */
    Optional<Job> findByUrl(String url);

    /**
     * Find jobs by title keyword (case-insensitive)
     */
    @Query("SELECT j FROM Job j WHERE LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Job> findByTitleKeyword(@Param("keyword") String keyword, Pageable pageable);

    /**
     * Find jobs by location (case-insensitive)
     */
    @Query("SELECT j FROM Job j WHERE LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Job> findByLocationKeyword(@Param("location") String location, Pageable pageable);

    /**
     * Search distinct job locations for autocomplete/dropdown support.
     */
    @Query("SELECT new com.entry_level_jobs.dto.LocationOption(j.location, COUNT(j)) FROM Job j " +
            "WHERE j.location IS NOT NULL AND (:query IS NULL OR :query = '' OR LOWER(j.location) LIKE LOWER(CONCAT('%', :query, '%'))) "
            +
            "GROUP BY j.location " +
            "ORDER BY COUNT(j) DESC, LOWER(j.location)")
    Page<LocationOption> searchLocations(@Param("query") String query, Pageable pageable);

    @Query("SELECT j FROM Job j WHERE LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "AND LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))")
    Page<Job> findByTitleAndLocationKeyword(@Param("keyword") String keyword,
            @Param("location") String location,
            Pageable pageable);
}
