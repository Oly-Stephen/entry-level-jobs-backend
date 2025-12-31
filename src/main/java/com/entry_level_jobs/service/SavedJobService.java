package com.entry_level_jobs.service;

import com.entry_level_jobs.model.Job;
import com.entry_level_jobs.model.SavedJob;
import com.entry_level_jobs.repository.JobRepository;
import com.entry_level_jobs.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Coordinates business logic for allowing a user to persist and retrieve their
 * saved jobs.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SavedJobService {
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;

    @Transactional
    public Job saveJobForUser(Long jobId, String userId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found: " + jobId));

        return savedJobRepository.findByUserIdAndJobId(userId, jobId)
                .map(SavedJob::getJob)
                .orElseGet(() -> {
                    SavedJob savedJob = SavedJob.builder()
                            .job(job)
                            .userId(userId)
                            .savedAt(LocalDateTime.now())
                            .build();
                    savedJobRepository.save(savedJob);
                    log.info("User {} saved job {}", userId, jobId);
                    return job;
                });
    }

    @Transactional(readOnly = true)
    public List<Job> getSavedJobsForUser(String userId) {
        return savedJobRepository.findJobsByUserIdOrderBySavedAtDesc(userId);
    }

    @Transactional
    public void removeSavedJob(Long jobId, String userId) {
        if (savedJobRepository.findByUserIdAndJobId(userId, jobId).isPresent()) {
            savedJobRepository.deleteByUserIdAndJobId(userId, jobId);
            log.info("User {} removed saved job {}", userId, jobId);
        }
    }
}
