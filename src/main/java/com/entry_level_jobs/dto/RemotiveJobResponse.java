package com.entry_level_jobs.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Remotive API response
 * Endpoint: https://remotive.com/api/remote-jobs
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class RemotiveJobResponse {
    private List<RemotiveJob> jobs;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RemotiveJob {
        private String id;
        private String url;
        private String title;
        @JsonProperty("company_name")
        private String companyName;
        @JsonProperty("candidate_required_location")
        private String candidateRequiredLocation;
        @JsonProperty("publication_date")
        private String publicationDate; // ISO 8601
        private String description;
        private String category;
        @JsonProperty("job_type")
        private String jobType;
    }
}
