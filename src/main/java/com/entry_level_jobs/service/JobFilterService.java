package com.entry_level_jobs.service;

import com.entry_level_jobs.dto.classification.ExperienceSignal;
import com.entry_level_jobs.dto.classification.JobClassificationScore;
import com.entry_level_jobs.language.JobLanguage;
import com.entry_level_jobs.language.JobLanguagePack;
import com.entry_level_jobs.language.LanguageKeywordRepository;
import com.entry_level_jobs.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for filtering jobs to identify entry-level positions.
 * Includes keywords: junior, entry level, trainee, intern, graduate, no
 * experience, 0-1 years
 * Excludes keywords: senior, lead, manager, 3+ years, mid-level
 */
@Service
@Slf4j
@RequiredArgsConstructor
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

    private static final int LANGUAGE_SCORE_THRESHOLD = 6;

    private final LanguageDetectionService languageDetectionService;
    private final ExperienceSignalExtractor experienceSignalExtractor;

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
                    JobClassificationScore classification = classifyJob(job);
                    if (classification.isEntryLevel()) {
                        log.debug("✓ PASSED: {} - {} ({}) | include={}, exclude={}, score={} (lang={}, hits={} / {})",
                                job.getTitle(), job.getCompany(), job.getSource(),
                                classification.isLegacyIncludeMatch(), classification.isLegacyExcludeMatch(),
                                classification.getTotalScore(), classification.getLanguage(),
                                classification.getPositiveKeywords(), classification.getNegativeKeywords());
                    } else {
                        log.debug("✗ FILTERED: {} - include={}, exclude={}, score={} (lang={})",
                                job.getTitle(), classification.isLegacyIncludeMatch(),
                                classification.isLegacyExcludeMatch(), classification.getTotalScore(),
                                classification.getLanguage());
                    }
                    return classification.isEntryLevel();
                })
                .collect(Collectors.toList());

        log.info("Filtered results: {} entry-level jobs out of {} total jobs", filtered.size(), jobs.size());
        return filtered;
    }

    public JobClassificationScore classifyJob(Job job) {
        String searchText = getSearchText(job);
        String normalizedText = searchText.toLowerCase(Locale.ROOT);

        JobLanguage language = languageDetectionService.detectLanguage(searchText);
        JobLanguagePack pack = LanguageKeywordRepository.PACKS
                .getOrDefault(language, LanguageKeywordRepository.PACKS.get(JobLanguage.EN));

        List<String> positiveHits = new ArrayList<>();
        List<String> negativeHits = new ArrayList<>();
        int keywordScore = 0;
        if (pack != null) {
            keywordScore += accumulateScore(normalizedText, pack.positive, positiveHits);
            keywordScore += accumulateScore(normalizedText, pack.negative, negativeHits);
        }

        List<ExperienceSignal> experienceSignals = experienceSignalExtractor.extractSignals(searchText, language);
        int experienceScore = experienceSignals.stream().mapToInt(ExperienceSignal::getWeight).sum();

        boolean legacyInclude = containsIncludeKeyword(normalizedText, job);
        boolean legacyExclude = containsExcludeKeyword(normalizedText, job);

        int totalScore = keywordScore + experienceScore;
        boolean entryLevel = (legacyInclude && !legacyExclude) || totalScore >= LANGUAGE_SCORE_THRESHOLD;

        return JobClassificationScore.builder()
                .language(language)
                .keywordScore(keywordScore)
                .experienceScore(experienceScore)
                .totalScore(totalScore)
                .legacyIncludeMatch(legacyInclude)
                .legacyExcludeMatch(legacyExclude)
                .entryLevel(entryLevel)
                .positiveKeywords(positiveHits)
                .negativeKeywords(negativeHits)
                .experienceSignals(experienceSignals)
                .build();
    }

    /**
     * Check if job contains any entry-level inclusion keywords.
     */
    private boolean containsIncludeKeyword(String normalizedText, Job job) {
        for (String keyword : INCLUDE_KEYWORDS) {
            if (normalizedText.contains(keyword.toLowerCase(Locale.ROOT))) {
                log.trace("Found include keyword '{}' in: {}", keyword, job.getTitle());
                return true;
            }
        }
        return false;
    }

    /**
     * Check if job contains any exclusion keywords.
     */
    private boolean containsExcludeKeyword(String normalizedText, Job job) {
        for (String keyword : EXCLUDE_KEYWORDS) {
            if (normalizedText.contains(keyword.toLowerCase(Locale.ROOT))) {
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
        String title = job.getTitle() == null ? "" : job.getTitle();
        String description = job.getDescription() == null ? "" : job.getDescription();
        return (title + " " + description).trim();
    }

    private int accumulateScore(String normalizedText, Map<String, Integer> keywords, List<String> hits) {
        if (keywords == null || keywords.isEmpty()) {
            return 0;
        }
        int score = 0;
        for (Map.Entry<String, Integer> entry : keywords.entrySet()) {
            String keyword = entry.getKey().toLowerCase(Locale.ROOT);
            if (normalizedText.contains(keyword)) {
                if (hits != null) {
                    hits.add(entry.getKey());
                }
                score += entry.getValue();
            }
        }
        return score;
    }
}
