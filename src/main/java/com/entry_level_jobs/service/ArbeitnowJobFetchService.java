package com.entry_level_jobs.service;

import com.entry_level_jobs.dto.ArbeitnowJobResponse;
import com.entry_level_jobs.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Service for fetching jobs from the Arbeitnow public API.
 * API: https://arbeitnow.com/api/job-board-api
 * No authentication required.
 */
@Service
@Slf4j
public class ArbeitnowJobFetchService {
    private static final String ARBEITNOW_API_URL = "https://arbeitnow.com/api/job-board-api?page=";
    private final RestTemplate restTemplate;

    // Use constructor injection so we can provide a configured RestTemplate bean
    public ArbeitnowJobFetchService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Fetch jobs from Arbeitnow API for a specific page.
     *
     * @param page Page number (1-based)
     * @return List of jobs from the API
     */
    public List<Job> fetchJobsFromArbeitnow(int page) {
        try {
            log.info("Fetching jobs from Arbeitnow API, page: {}", page);
            String url = ARBEITNOW_API_URL + page;

            ArbeitnowJobResponse response = restTemplate.getForObject(url, ArbeitnowJobResponse.class);

            if (response == null || response.getData() == null) {
                log.warn("No data received from Arbeitnow API");
                return new ArrayList<>();
            }

            List<Job> jobs = new ArrayList<>();
            for (ArbeitnowJobResponse.ArbeitnowJob arbeitJob : response.getData()) {
                Job job = convertArbeitnowJobToJob(arbeitJob);
                jobs.add(job);
            }

            log.info("Successfully fetched {} jobs from Arbeitnow API (page {})", jobs.size(), page);
            return jobs;
        } catch (Exception e) {
            log.error("Error fetching jobs from Arbeitnow API", e);
            return new ArrayList<>();
        }
    }

    /**
     * Convert Arbeitnow job to internal Job entity.
     */
    private Job convertArbeitnowJobToJob(ArbeitnowJobResponse.ArbeitnowJob arbeitJob) {
        Long createdEpoch = arbeitJob.getCreated_at();
        if (createdEpoch == null) {
            createdEpoch = Instant.now().getEpochSecond();
        }
        LocalDateTime postedAt = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(createdEpoch),
                ZoneId.systemDefault());

        return Job.builder()
                .title(arbeitJob.getTitle())
                .company(arbeitJob.getCompany_name())
                .location(arbeitJob.getLocation())
                .url(arbeitJob.getUrl())
                .description(stripHtmlTags(arbeitJob.getDescription()))
                .source("Arbeitnow")
                .postedAt(postedAt)
                .build();
    }

    /**
     * Strip HTML tags from description for cleaner storage.
     */
    private String stripHtmlTags(String html) {
        if (html == null) {
            return "";
        }
        return html.replaceAll("<[^>]*>", "").trim();
    }
}
