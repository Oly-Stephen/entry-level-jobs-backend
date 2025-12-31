package com.entry_level_jobs.repository;

import com.entry_level_jobs.model.Job;
import com.entry_level_jobs.model.SavedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for user-saved jobs. Provides helpers to avoid duplicate saves and
 * fetch a user's saved items ordered by recency.
 */
@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, Long> {
    Optional<SavedJob> findByUserIdAndJobId(String userId, Long jobId);

    void deleteByUserIdAndJobId(String userId, Long jobId);

    @Query("SELECT sj.job FROM SavedJob sj WHERE sj.userId = :userId ORDER BY sj.savedAt DESC")
    List<Job> findJobsByUserIdOrderBySavedAtDesc(@Param("userId") String userId);
}
