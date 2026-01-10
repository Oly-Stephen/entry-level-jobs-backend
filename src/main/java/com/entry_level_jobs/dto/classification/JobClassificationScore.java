package com.entry_level_jobs.dto.classification;

import com.entry_level_jobs.language.JobLanguage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * Debug-friendly classification payload that surfaces how the system evaluated
 * a job posting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobClassificationScore {
    @JsonProperty("language")
    private JobLanguage language;

    @JsonProperty("keyword_score")
    private int keywordScore;

    @JsonProperty("experience_score")
    private int experienceScore;

    @JsonProperty("total_score")
    private int totalScore;

    @JsonProperty("legacy_include_match")
    private boolean legacyIncludeMatch;

    @JsonProperty("legacy_exclude_match")
    private boolean legacyExcludeMatch;

    @JsonProperty("entry_level")
    private boolean entryLevel;

    @JsonProperty("positive_keywords")
    @Builder.Default
    private List<String> positiveKeywords = Collections.emptyList();

    @JsonProperty("negative_keywords")
    @Builder.Default
    private List<String> negativeKeywords = Collections.emptyList();

    @JsonProperty("experience_signals")
    @Builder.Default
    private List<ExperienceSignal> experienceSignals = Collections.emptyList();
}
