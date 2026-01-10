package com.entry_level_jobs.service;

import com.entry_level_jobs.model.Job;
import com.entry_level_jobs.repository.JobRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Scheduler service that periodically fetches jobs from external APIs and saves
 * entry-level jobs.
 */
@Service
@Slf4j
@ConditionalOnProperty(value = "jobs.fetch.scheduling.enabled", havingValue = "true", matchIfMissing = true)
public class JobSchedulerService {
    private final JobFetchService jobFetchService;
    private final JobFilterService jobFilterService;
    private final JobRepository jobRepository;
    private final ReentrantLock runLock = new ReentrantLock();
    private final long serviceDelayMs;

    public JobSchedulerService(JobFetchService jobFetchService,
            JobFilterService jobFilterService,
            JobRepository jobRepository,
            @Value("${jobs.fetch.service-delay-ms:500}") long serviceDelayMs) {
        this.jobFetchService = jobFetchService;
        this.jobFilterService = jobFilterService;
        this.jobRepository = jobRepository;
        this.serviceDelayMs = serviceDelayMs;
    }

    // Runs every jobs.fetch.interval.ms milliseconds (default 1 hour)
    @Scheduled(fixedDelayString = "${jobs.fetch.interval.ms:3600000}")
    public void scheduledFetchAndSave() {
        if (!runLock.tryLock()) {
            log.info("Another fetch job is already running; skipping this run");
            return;
        }

        try {
            log.info("Scheduled job started: fetching jobs from external APIs");
            List<Job> fetched = jobFetchService.fetchJobsFromApis();

            // small delay between services to be gentle on APIs
            try {
                Thread.sleep(serviceDelayMs);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }

            List<Job> filtered = jobFilterService.filterEntryLevelJobs(fetched);
            saveJobsTransactional(filtered);
            log.info("Scheduled job completed: saved entry-level jobs");
        } catch (Exception e) {
            log.error("Error during scheduled fetch and save", e);
        } finally {
            runLock.unlock();
        }
    }

    @Transactional
    public void saveJobsTransactional(List<Job> jobs) {
        for (Job job : jobs) {
            try {
                if (job.getUrl() != null && !job.getUrl().isBlank()) {
                    if (jobRepository.findByUrl(job.getUrl()).isPresent()) {
                        log.debug("Job with URL already exists, skipping: {}", job.getUrl());
                        continue;
                    }
                }
                jobRepository.save(job);
                log.debug("Saved job: {} - {}", job.getTitle(), job.getCompany());
            } catch (DataIntegrityViolationException dive) {
                // Likely unique constraint violation due to race; ignore and continue
                log.warn("Data integrity violation when saving job (possible duplicate), skipping: {}", job.getUrl());
            } catch (Exception e) {
                log.error("Failed to save job: {}", job.getUrl(), e);
            }
        }
    }
}
