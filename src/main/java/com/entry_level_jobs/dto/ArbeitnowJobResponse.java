package com.entry_level_jobs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Arbeitnow API response structure.
 * Represents the job data from the public Arbeitnow API.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArbeitnowJobResponse {
    private List<ArbeitnowJob> data;
    private ArbeitnowLinks links;
    private ArbeitnowMeta meta;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArbeitnowJob {
        private String slug;
        private String company_name;
        private String title;
        private String description;
        private Boolean remote;
        private String url;
        private List<String> tags;
        private List<String> job_types;
        private String location;
        // Use a custom deserializer to handle either numeric epoch seconds or ISO date strings
        @JsonDeserialize(using = CreatedAtDeserializer.class)
        private Long created_at;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArbeitnowLinks {
        private String first;
        private String last;
        private String prev;
        private String next;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ArbeitnowMeta {
        private Integer current_page;
        private Integer from;
        private String path;
        private Integer per_page;
        private Integer to;
        private String terms;
        private String info;
    }
}
