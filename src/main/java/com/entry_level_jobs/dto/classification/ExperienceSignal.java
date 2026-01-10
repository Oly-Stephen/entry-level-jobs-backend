package com.entry_level_jobs.dto.classification;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a phrase detected in the job text that indicates a specific
 * experience expectation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExperienceSignal {
    @JsonProperty("phrase")
    private String phrase;

    @JsonProperty("type")
    private ExperienceSignalType type;

    @JsonProperty("weight")
    private int weight;
}
