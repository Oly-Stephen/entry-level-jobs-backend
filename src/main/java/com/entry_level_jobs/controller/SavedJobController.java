package com.entry_level_jobs.controller;

import com.entry_level_jobs.model.Job;
import com.entry_level_jobs.service.SavedJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API endpoints that allow an authenticated user to persist jobs for later
 * review.
 */
@RestController
@RequestMapping("/api/saved-jobs")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class SavedJobController {

    private final SavedJobService savedJobService;

    @PostMapping("/{jobId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Job> saveJob(@PathVariable Long jobId, Authentication authentication) {
        String userId = authentication.getName();
        try {
            Job job = savedJobService.saveJobForUser(jobId, userId);
            return ResponseEntity.ok(job);
        } catch (IllegalArgumentException ex) {
            log.warn("User {} attempted to save non-existent job {}", userId, jobId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<Job>> getSavedJobs(Authentication authentication) {
        String userId = authentication.getName();
        List<Job> savedJobs = savedJobService.getSavedJobsForUser(userId);
        return ResponseEntity.ok(savedJobs);
    }

    @DeleteMapping("/{jobId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteSavedJob(@PathVariable Long jobId, Authentication authentication) {
        String userId = authentication.getName();
        savedJobService.removeSavedJob(jobId, userId);
        return ResponseEntity.noContent().build();
    }
}
