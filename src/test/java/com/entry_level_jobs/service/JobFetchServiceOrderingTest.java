package com.entry_level_jobs.service;

import com.entry_level_jobs.model.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobFetchServiceOrderingTest {

    @Mock
    private ArbeitnowJobFetchService arbeitnowJobFetchService;

    @Mock
    private RemotiveJobFetchService remotiveJobFetchService;

    @Mock
    private MuseJobFetchService museJobFetchService;

    private JobFetchService jobFetchService;

    @BeforeEach
    void setUp() {
        jobFetchService = new JobFetchService(arbeitnowJobFetchService, remotiveJobFetchService, museJobFetchService);
    }

    @Test
    void fetchJobsFromApisReturnsJobsSortedByPostedDate() {
        LocalDateTime now = LocalDateTime.now();
        Job remotiveJob = Job.builder()
                .title("Remotive role")
                .company("Remotive")
                .url("https://remotive.example/job")
                .postedAt(now)
                .build();
        Job museJob = Job.builder()
                .title("Muse role")
                .company("Muse")
                .url("https://muse.example/job")
                .postedAt(now.minusDays(1))
                .build();
        Job arbeitnowJob = Job.builder()
                .title("Arbeitnow role")
                .company("Arbeitnow")
                .url("https://arbeitnow.example/job")
                .postedAt(now.minusDays(2))
                .build();

        when(arbeitnowJobFetchService.fetchJobsFromArbeitnow(1)).thenReturn(List.of(arbeitnowJob));
        when(arbeitnowJobFetchService.fetchJobsFromArbeitnow(2)).thenReturn(Collections.emptyList());
        when(remotiveJobFetchService.fetchJobsFromRemotive()).thenReturn(List.of(remotiveJob));
        when(museJobFetchService.fetchJobsFromMuse()).thenReturn(List.of(museJob));

        List<Job> result = jobFetchService.fetchJobsFromApis();

        assertIterableEquals(List.of(remotiveJob, museJob, arbeitnowJob), result,
                "Jobs should be ordered by most recent postedAt timestamp");
    }
}
