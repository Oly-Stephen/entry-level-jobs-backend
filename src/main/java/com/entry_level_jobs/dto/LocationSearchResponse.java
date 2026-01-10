package com.entry_level_jobs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * Response wrapper for the /api/jobs/locations endpoint. Designed to power a
 * searchable select input on the client while remaining backwards compatible
 * with the legacy "locations" string array consumers relied upon.
 */
@Data
@Builder
public class LocationSearchResponse {
    @JsonProperty("success")
    private boolean success;

    @JsonProperty("query")
    private String query;

    @JsonProperty("options")
    private List<LocationOption> options;

    @JsonProperty("locations")
    private List<String> locations;

    @JsonProperty("returned")
    private int returned;

    @JsonProperty("total_matches")
    private long totalMatches;

    @JsonProperty("message")
    private String message;

    public static LocationSearchResponse empty(String query) {
        return LocationSearchResponse.builder()
                .success(true)
                .query(query)
                .options(Collections.emptyList())
                .locations(Collections.emptyList())
                .returned(0)
                .totalMatches(0)
                .message(
                        "We haven't indexed entry-level roles for that location yet. Try a nearby city or remote filter.")
                .build();
    }
}
