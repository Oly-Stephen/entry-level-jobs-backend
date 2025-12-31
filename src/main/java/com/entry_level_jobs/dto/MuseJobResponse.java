package com.entry_level_jobs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for The Muse API response
 * Endpoint: https://www.themuse.com/api/public/jobs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class MuseJobResponse {
    private List<MuseJob> results;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MuseJob {
        private String name;
        @JsonProperty("publication_date")
        private String publicationDate; // ISO 8601
        private Refs refs;
        private Company company;
        private List<Location> locations;
        private String contents;
        @JsonProperty("short_description")
        private String shortDescription;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Refs {
            @JsonProperty("landing_page")
            private String landingPage;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Company {
            private String name;
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        @Builder
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Location {
            private String name;
        }
    }
}

