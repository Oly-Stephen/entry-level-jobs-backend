package com.entry_level_jobs.service;

import com.entry_level_jobs.model.Job;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for filtering jobs to identify entry-level positions.
 * Includes keywords: junior, entry level, trainee, intern, graduate, no
 * experience, 0-1 years
 * Excludes keywords: senior, lead, manager, 3+ years, mid-level
 */
@Service
@Slf4j
public class JobFilterService {
    private static final String[] INCLUDE_KEYWORDS = {
            "entry level", "entry-level", "junior", "trainee", "intern",
            "graduate", "no experience", "0–1 years", "0 - 1 years",
            "apprentice", "newly graduated"
    };

    private static final String[] EXCLUDE_KEYWORDS = {
            "3+ years", "4+ years", "5+ years", "senior", "lead",
            "manager", "mid-level", "mid level", "experienced", "professional"
    };

    /**
     * Filter jobs to return only entry-level positions.
     *
     * @param jobs List of jobs to filter
     * @return Filtered list containing only entry-level jobs
     */
    public List<Job> filterEntryLevelJobs(List<Job> jobs) {
        log.debug("Starting to filter {} jobs for entry-level positions", jobs.size());

        List<Job> filtered = jobs.stream()
                .filter(job -> {
                    boolean hasIncludeKeyword = containsIncludeKeyword(job);
                    boolean hasExcludeKeyword = containsExcludeKeyword(job);
                    boolean isEntryLevel = hasIncludeKeyword && !hasExcludeKeyword;

                    if (isEntryLevel) {
                        log.debug("✓ PASSED: {} - {} ({})", job.getTitle(), job.getCompany(), job.getSource());
                    } else {
                        log.debug("✗ FILTERED: {} - Has include: {}, Has exclude: {}",
                                job.getTitle(), hasIncludeKeyword, hasExcludeKeyword);
                    }
                    return isEntryLevel;
                })
                .collect(Collectors.toList());

        log.info("Filtered results: {} entry-level jobs out of {} total jobs", filtered.size(), jobs.size());
        return filtered;
    }

    /**
     * Check if job contains any entry-level inclusion keywords.
     */
    private boolean containsIncludeKeyword(Job job) {
        String text = getSearchText(job).toLowerCase();
        for (String keyword : INCLUDE_KEYWORDS) {
            if (text.contains(keyword.toLowerCase())) {
                log.trace("Found include keyword '{}' in: {}", keyword, job.getTitle());
                return true;
            }
        }
        return false;
    }

    /**
     * Check if job contains any exclusion keywords.
     */
    private boolean containsExcludeKeyword(Job job) {
        String text = getSearchText(job).toLowerCase();
        for (String keyword : EXCLUDE_KEYWORDS) {
            if (text.contains(keyword.toLowerCase())) {
                log.trace("Found exclude keyword '{}' in: {}", keyword, job.getTitle());
                return true;
            }
        }
        return false;
    }

    /**
     * Get combined text from title and description for searching.
     */
    private String getSearchText(Job job) {
        return (job.getTitle() + " " + job.getDescription()).trim();
    }
}
