package com.entry_level_jobs.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Job entity representing a job listing.
 * Persisted to PostgreSQL database with unique URL constraint.
 */
@Entity
@Table(name = "jobs", uniqueConstraints = { @UniqueConstraint(columnNames = "url") })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Job title is required")
    @Column(length = 500)
    private String title;

    @NotBlank(message = "Company name is required")
    @Column(length = 255)
    private String company;

    @NotBlank(message = "Location is required")
    @Column(length = 255)
    private String location;

    @NotBlank(message = "Job URL is required")
    @Column(unique = true, nullable = false, length = 2000)
    private String url;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String source;

    @NotNull(message = "Posted date is required")
    private LocalDateTime postedAt;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
