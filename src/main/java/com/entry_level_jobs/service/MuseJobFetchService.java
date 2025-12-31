package com.entry_level_jobs.service;

import com.entry_level_jobs.dto.MuseJobResponse;
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
 * Service for fetching jobs from The Muse API
 */
@Service
@Slf4j
public class MuseJobFetchService {
    private final RestTemplate restTemplate;
    private final String museUrl;
    private final int pagesToFetch;
    private final int maxRetries;
    private final long initialBackoffMs;

    public MuseJobFetchService(RestTemplate restTemplate,
                               @Value("${external.themuse.url:https://www.themuse.com/api/public/jobs}") String museUrl,
                               @Value("${external.themuse.pages:3}") int pagesToFetch,
                               @Value("${external.fetch.max-retries:3}") int maxRetries,
                               @Value("${external.fetch.backoff.initial-ms:1000}") long initialBackoffMs) {
        this.restTemplate = restTemplate;
        this.museUrl = museUrl;
        this.pagesToFetch = pagesToFetch;
        this.maxRetries = maxRetries;
        this.initialBackoffMs = initialBackoffMs;
    }

    public List<Job> fetchJobsFromMuse() {
        List<Job> all = new ArrayList<>();
        for (int page = 1; page <= pagesToFetch; page++) {
            int attempt = 0;
            while (attempt < maxRetries) {
                try {
                    String url = museUrl + "?page=" + page;
                    log.info("Fetching Muse jobs from {} (attempt {})", url, attempt + 1);
                    MuseJobResponse response = restTemplate.getForObject(url, MuseJobResponse.class);
                    if (response == null || response.getResults() == null) {
                        log.warn("No data received from Muse API for page {}", page);
                        break;
                    }
                    for (MuseJobResponse.MuseJob mj : response.getResults()) {
                        Job job = convertMuseToJob(mj);
                        all.add(job);
                    }
                    break; // success, break retry loop
                } catch (HttpClientErrorException.TooManyRequests tre) {
                    log.warn("Muse returned 429, backing off", tre);
                } catch (Exception e) {
                    log.error("Error fetching from Muse", e);
                }

                attempt++;
                try {
                    Thread.sleep(initialBackoffMs * (1L << (attempt - 1)));
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        log.info("Fetched {} jobs from Muse (pages {})", all.size(), pagesToFetch);
        return all;
    }

    private Job convertMuseToJob(MuseJobResponse.MuseJob mj) {
        LocalDateTime postedAt = parseIsoToLocal(mj.getPublicationDate());
        String url = null;
        if (mj.getRefs() != null) {
            url = mj.getRefs().getLandingPage();
        }
        String company = mj.getCompany() != null ? mj.getCompany().getName() : "";
        String location = "Remote";
        if (mj.getLocations() != null && !mj.getLocations().isEmpty()) {
            String loc = mj.getLocations().get(0).getName();
            if (loc != null && !loc.isBlank()) location = loc;
        }
        String description = mj.getContents() != null ? mj.getContents() : mj.getShortDescription();

        return Job.builder()
                .title(mj.getName())
                .company(company)
                .location(location)
                .url(url)
                .description(stripHtmlTags(description))
                .source("TheMuse")
                .postedAt(postedAt != null ? postedAt : LocalDateTime.now())
                .build();
    }

    private LocalDateTime parseIsoToLocal(String iso) {
        if (iso == null || iso.isBlank()) return null;
        try {
            Instant inst = Instant.parse(iso);
            return LocalDateTime.ofInstant(inst, ZoneId.systemDefault());
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse ISO date from Muse: {}", iso);
            return null;
        }
    }

    private String stripHtmlTags(String html) {
        if (html == null) return "";
        return html.replaceAll("<[^>]*>", "").trim();
    }
}

