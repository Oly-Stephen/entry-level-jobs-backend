package com.entry_level_jobs.service;

import com.entry_level_jobs.dto.ArbeitnowJobResponse;
import com.entry_level_jobs.model.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class ArbeitnowJobFetchServiceTest {
    private RestTemplate restTemplate;
    private ArbeitnowJobFetchService service;

    @BeforeEach
    public void setup() {
        restTemplate = Mockito.mock(RestTemplate.class);
        service = new ArbeitnowJobFetchService(restTemplate);
    }

    @Test
    public void testFetchJobsSuccess() {
        ArbeitnowJobResponse.ArbeitnowJob job = ArbeitnowJobResponse.ArbeitnowJob.builder()
                .title("Dev")
                .company_name("ACME")
                .url("https://example.com/j/1")
                .location("Remote")
                .created_at(1700000000L)
                .description("<p>Hello</p>")
                .build();

        ArbeitnowJobResponse resp = ArbeitnowJobResponse.builder().data(List.of(job)).build();
        when(restTemplate.getForObject(anyString(), eq(ArbeitnowJobResponse.class))).thenReturn(resp);

        List<Job> jobs = service.fetchJobsFromArbeitnow(1);
        assertNotNull(jobs);
        assertEquals(1, jobs.size());
        Job j = jobs.get(0);
        assertEquals("Dev", j.getTitle());
        assertEquals("ACME", j.getCompany());
        assertEquals("Remote", j.getLocation());
        assertEquals("https://example.com/j/1", j.getUrl());
        assertEquals("Hello", j.getDescription());
    }

    @Test
    public void testFetchJobsHandlesNullResponse() {
        when(restTemplate.getForObject(anyString(), eq(ArbeitnowJobResponse.class))).thenReturn(null);
        List<Job> jobs = service.fetchJobsFromArbeitnow(1);
        assertNotNull(jobs);
        assertTrue(jobs.isEmpty());
    }
}

