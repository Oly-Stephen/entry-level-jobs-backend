package com.entry_level_jobs.controller;

import com.entry_level_jobs.dto.JobWithClassificationDTO;
import com.entry_level_jobs.dto.LocationOption;
import com.entry_level_jobs.dto.LocationSearchResponse;
import com.entry_level_jobs.dto.PaginatedResponse;
import com.entry_level_jobs.model.Job;
import com.entry_level_jobs.repository.JobRepository;
import com.entry_level_jobs.service.JobFetchService;
import com.entry_level_jobs.service.JobFilterService;
import com.entry_level_jobs.service.PaginationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST Controller for job management endpoints.
 * Provides endpoints for:
 * - Fetching and saving jobs from external sources
 * - Retrieving saved jobs with filtering and pagination
 * 
 * Follows clean architecture principles by delegating business logic to
 * services.
 */
@RestController
@RequestMapping("/api/jobs")
@CrossOrigin(origins = "*")
@Slf4j
public class JobController {
    private final JobRepository jobRepository;
    private final JobFetchService jobFetchService;
    private final JobFilterService jobFilterService;
    private final PaginationService paginationService;

    public JobController(JobRepository jobRepository, JobFetchService jobFetchService,
            JobFilterService jobFilterService, PaginationService paginationService) {
        this.jobRepository = jobRepository;
        this.jobFetchService = jobFetchService;
        this.jobFilterService = jobFilterService;
        this.paginationService = paginationService;
    }

    /**
     * Get all jobs or filter by keyword/location with pagination.
     * GET /api/jobs?page=0&size=10
     * GET /api/jobs?keyword=java&page=0&size=10
     * GET /api/jobs?location=Remote&page=0&size=10
     * 
     * @param keyword  Optional keyword filter for job title
     * @param location Optional location filter
     * @param page     Page number (0-indexed), default 0
     * @param size     Page size, default 10, max 100
     * @return PaginatedResponse with jobs and pagination metadata
     */
    @GetMapping
    public ResponseEntity<PaginatedResponse<JobWithClassificationDTO>> getAllJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String keywordFilter = normalizeFilterValue(keyword);
        String locationFilter = normalizeFilterValue(location);
        boolean filterByKeyword = keywordFilter != null;
        boolean filterByLocation = locationFilter != null;

        log.info("Retrieving jobs with pagination: keyword={}, location={}, page={}, size={}",
                keywordFilter, locationFilter, page, size);

        try {
            // Validate and sanitize pagination parameters
            if (!paginationService.validatePaginationParams(page, size)) {
                log.warn("Invalid pagination parameters: page={}, size={}", page, size);
                return ResponseEntity.badRequest().body(
                        PaginatedResponse.<JobWithClassificationDTO>error("Invalid pagination parameters",
                                "Page must be >= 0 and size must be between 1 and 100"));
            }

            size = paginationService.sanitizePageSize(size);
            Sort sort = Sort.by(Sort.Direction.DESC, "postedAt", "createdAt", "id");
            PageRequest pageRequest = PageRequest.of(page, size, sort);
            Page<Job> jobsPage;

            // Fetch based on filters
            if (filterByKeyword && filterByLocation) {
                log.debug("Filtering jobs by keyword={} and location={}", keywordFilter, locationFilter);
                jobsPage = jobRepository.findByTitleAndLocationKeyword(keywordFilter, locationFilter, pageRequest);
            } else if (filterByKeyword) {
                log.debug("Filtering jobs by keyword: {}", keywordFilter);
                jobsPage = jobRepository.findByTitleKeyword(keywordFilter, pageRequest);
            } else if (filterByLocation) {
                log.debug("Filtering jobs by location: {}", locationFilter);
                jobsPage = jobRepository.findByLocationKeyword(locationFilter, pageRequest);
            } else {
                log.debug("Retrieving all jobs");
                jobsPage = jobRepository.findAll(pageRequest);
            }

            log.info("Found {} jobs on page {} (total: {})",
                    jobsPage.getNumberOfElements(), page, jobsPage.getTotalElements());

            // Convert to paginated response using service
            List<JobWithClassificationDTO> jobDtos = jobsPage.getContent().stream()
                    .map(job -> JobWithClassificationDTO.from(job, jobFilterService.classifyJob(job)))
                    .collect(Collectors.toList());

            PaginatedResponse<JobWithClassificationDTO> response = PaginatedResponse.success(
                    jobDtos,
                    paginationService.toPaginationDTO(jobsPage));

            if (filterByLocation) {
                if (jobsPage.getTotalElements() > 0) {
                    response.setMessage(buildLocationResultsMessage(locationFilter,
                            filterByKeyword ? keywordFilter : null));
                } else {
                    response.setMessage(buildNoLocationResultsMessage(locationFilter));
                }
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error retrieving jobs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    PaginatedResponse.<JobWithClassificationDTO>error(
                            "Error retrieving jobs",
                            e.getMessage()));
        }
    }

    /**
     * Provide a lightweight list of distinct locations for search/autocomplete.
     * GET /api/jobs/locations?query=lagos&limit=5
     */
    @GetMapping("/locations")
    public ResponseEntity<LocationSearchResponse> searchLocations(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "10") int limit) {
        int sanitizedLimit = Math.max(1, Math.min(limit, 50));
        String normalizedQuery = normalizeFilterValue(query);
        PageRequest pageRequest = PageRequest.of(0, sanitizedLimit);

        Page<LocationOption> locations = jobRepository.searchLocations(normalizedQuery, pageRequest);

        if (locations.isEmpty()) {
            return ResponseEntity.ok(LocationSearchResponse.empty(normalizedQuery));
        }

        List<LocationOption> options = locations.getContent();
        List<String> legacyLocations = options.stream()
                .map(LocationOption::getValue)
                .collect(Collectors.toList());

        LocationSearchResponse response = LocationSearchResponse.builder()
                .success(true)
                .query(normalizedQuery)
                .options(options)
                .locations(legacyLocations)
                .returned(locations.getNumberOfElements())
                .totalMatches(locations.getTotalElements())
                .message("Select a location to narrow entry-level opportunities.")
                .build();
        return ResponseEntity.ok(response);
    }

    /**
     * Fetch jobs from external APIs and save entry-level jobs to database.
     * POST /api/jobs/fetch
     */
    @PostMapping("/fetch")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> fetchAndSaveJobs() {
        log.info("Starting job fetch and save process");
        Map<String, Object> response = new HashMap<>();

        try {
            // Fetch jobs from external sources
            List<Job> fetchedJobs = jobFetchService.fetchJobsFromApis();
            log.info("Fetched {} jobs from external APIs", fetchedJobs.size());

            // Filter for entry-level jobs
            List<Job> entryLevelJobs = jobFilterService.filterEntryLevelJobs(fetchedJobs);
            log.info("Filtered down to {} entry-level jobs", entryLevelJobs.size());

            // Save jobs that don't already exist
            int saved = 0;
            int duplicates = 0;
            for (Job job : entryLevelJobs) {
                if (jobRepository.findByUrl(job.getUrl()).isEmpty()) {
                    jobRepository.save(job);
                    log.info("Saved job: {} from {}", job.getTitle(), job.getSource());
                    saved++;
                } else {
                    log.debug("Skipped duplicate job: {}", job.getTitle());
                    duplicates++;
                }
            }

            response.put("success", true);
            response.put("message",
                    String.format("Process completed: %d new jobs saved, %d duplicates skipped", saved, duplicates));
            response.put("saved", saved);
            response.put("duplicates", duplicates);
            response.put("total_filtered", entryLevelJobs.size());
            response.put("total_fetched", fetchedJobs.size());

            log.info("Job fetch process completed successfully: saved={}, duplicates={}", saved, duplicates);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during job fetch process", e);
            response.put("success", false);
            response.put("error", "Error during fetch process: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Test endpoint to verify database connection and job saving.
     * POST /api/jobs/test-save
     */
    @PostMapping("/test-save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> testSave() {
        log.info("Testing database connection and job save");
        Map<String, Object> response = new HashMap<>();

        try {
            Job testJob = Job.builder()
                    .title("Test Junior Position")
                    .company("Test Company")
                    .location("Remote")
                    .url("https://test.example.com/jobs/test-" + System.currentTimeMillis())
                    .description("This is a test entry level position for testing the application.")
                    .source("TestSource")
                    .postedAt(LocalDateTime.now())
                    .build();

            Job savedJob = jobRepository.save(testJob);
            log.info("Test job saved successfully with ID: {}", savedJob.getId());

            response.put("success", true);
            response.put("message", "Test job saved successfully");
            response.put("job", savedJob);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error saving test job", e);
            response.put("success", false);
            response.put("error", "Error saving test job: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get application health and statistics.
     * GET /api/jobs/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() {
        log.info("Retrieving job statistics");
        Map<String, Object> response = new HashMap<>();

        try {
            long totalJobs = jobRepository.count();
            response.put("success", true);
            response.put("total_jobs", totalJobs);
            response.put("timestamp", LocalDateTime.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error retrieving statistics", e);
            response.put("success", false);
            response.put("error", "Error retrieving statistics: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String normalizeFilterValue(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String buildNoLocationResultsMessage(String location) {
        return String.format(
                "We couldn't find entry-level opportunities in %s yet. Try remote-friendly or nearby locations.",
                location);
    }

    private String buildLocationResultsMessage(String location, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return String.format("Showing entry-level opportunities available in %s.", location);
        }
        return String.format("Showing entry-level opportunities in %s matching \"%s\".",
                location, keyword);
    }
}
