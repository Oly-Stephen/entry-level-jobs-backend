package com.entry_level_jobs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight location option used by the location search endpoint. Provides
 * enough metadata for search inputs/autocomplete widgets to display useful
 * information without pulling entire job payloads.
 */
@Data
@NoArgsConstructor
public class LocationOption {
    @JsonProperty("value")
    private String value;

    @JsonProperty("label")
    private String label;

    @JsonProperty("job_count")
    private long jobCount;

    public LocationOption(String location, Long jobCount) {
        this.value = location;
        this.label = location;
        this.jobCount = jobCount == null ? 0L : jobCount;
    }
}
