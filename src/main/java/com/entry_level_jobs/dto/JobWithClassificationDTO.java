package com.entry_level_jobs.dto;

import com.entry_level_jobs.dto.classification.JobClassificationScore;
import com.entry_level_jobs.model.Job;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO that mirrors the Job entity shape while adding classification metadata
 * for debugging purposes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobWithClassificationDTO {
    @JsonProperty("id")
    private Long id;

    @JsonProperty("title")
    private String title;

    @JsonProperty("company")
    private String company;

    @JsonProperty("location")
    private String location;

    @JsonProperty("url")
    private String url;

    @JsonProperty("description")
    private String description;

    @JsonProperty("source")
    private String source;

    @JsonProperty("postedAt")
    private LocalDateTime postedAt;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;

    @JsonProperty("classification")
    private JobClassificationScore classification;

    public static JobWithClassificationDTO from(Job job, JobClassificationScore classification) {
        return JobWithClassificationDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(job.getCompany())
                .location(job.getLocation())
                .url(job.getUrl())
                .description(job.getDescription())
                .source(job.getSource())
                .postedAt(job.getPostedAt())
                .createdAt(job.getCreatedAt())
                .classification(classification)
                .build();
    }
}
