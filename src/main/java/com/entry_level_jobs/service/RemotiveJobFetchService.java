package com.entry_level_jobs.service;

import com.entry_level_jobs.dto.RemotiveJobResponse;
import com.entry_level_jobs.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for fetching jobs from Remotive API
 */
@Service
@Slf4j
public class RemotiveJobFetchService {
    private final RestTemplate restTemplate;
    private final String remotiveUrl;
    private final int maxRetries;
    private final long initialBackoffMs;

    public RemotiveJobFetchService(RestTemplate restTemplate,
            @Value("${external.remotive.url:https://remotive.com/api/remote-jobs}") String remotiveUrl,
            @Value("${external.fetch.max-retries:3}") int maxRetries,
            @Value("${external.fetch.backoff.initial-ms:1000}") long initialBackoffMs) {
        this.restTemplate = restTemplate;
        this.remotiveUrl = remotiveUrl;
        this.maxRetries = maxRetries;
        this.initialBackoffMs = initialBackoffMs;
    }

    public List<Job> fetchJobsFromRemotive() {
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                log.info("Fetching jobs from Remotive API (attempt {})", attempt + 1);
                RemotiveJobResponse response = restTemplate.getForObject(remotiveUrl, RemotiveJobResponse.class);
                if (response == null || response.getJobs() == null) {
                    log.warn("No data received from Remotive API");
                    return new ArrayList<>();
                }
                List<Job> jobs = new ArrayList<>();
                for (RemotiveJobResponse.RemotiveJob rj : response.getJobs()) {
                    Job job = convertRemotiveToJob(rj);
                    jobs.add(job);
                }
                log.info("Fetched {} jobs from Remotive", jobs.size());
                return jobs;
            } catch (HttpClientErrorException.TooManyRequests tre) {
                log.warn("Remotive returned 429 Too Many Requests, backing off", tre);
            } catch (Exception e) {
                log.error("Error fetching from Remotive", e);
            }

            attempt++;
            try {
                Thread.sleep(initialBackoffMs * (1L << (attempt - 1)));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        return new ArrayList<>();
    }

    private Job convertRemotiveToJob(RemotiveJobResponse.RemotiveJob rj) {
        LocalDateTime postedAt = parseIsoToLocal(rj.getPublicationDate());
        String location = rj.getCandidateRequiredLocation();
        if (location == null || location.isBlank()) {
            location = "Remote";
        }
        return Job.builder()
                .title(rj.getTitle())
                .company(rj.getCompanyName() != null ? rj.getCompanyName() : "")
                .location(location)
                .url(rj.getUrl())
                .description(stripHtmlTags(rj.getDescription()))
                .source("Remotive")
                .postedAt(postedAt != null ? postedAt : LocalDateTime.now())
                .build();
    }

    private LocalDateTime parseIsoToLocal(String iso) {
        if (iso == null || iso.isBlank())
            return null;
        try {
            Instant inst = Instant.parse(iso);
            return LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse ISO date from Remotive: {}", iso);
            return null;
        }
    }

    private String stripHtmlTags(String html) {
        if (html == null)
            return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }
}
