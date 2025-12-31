package com.entry_level_jobs.service;

import com.entry_level_jobs.model.Job;
import com.entry_level_jobs.model.SavedJob;
import com.entry_level_jobs.repository.JobRepository;
import com.entry_level_jobs.repository.SavedJobRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavedJobServiceTest {

    @Mock
    private SavedJobRepository savedJobRepository;

    @Mock
    private JobRepository jobRepository;

    @InjectMocks
    private SavedJobService savedJobService;

    @Test
    void saveJobForUserPersistsWhenNotAlreadySaved() {
        Job job = Job.builder().id(1L).title("Junior Developer").build();
        when(jobRepository.findById(1L)).thenReturn(Optional.of(job));
        when(savedJobRepository.findByUserIdAndJobId("user", 1L)).thenReturn(Optional.empty());

        Job result = savedJobService.saveJobForUser(1L, "user");

        assertEquals(job, result);
        ArgumentCaptor<SavedJob> captor = ArgumentCaptor.forClass(SavedJob.class);
        verify(savedJobRepository).save(captor.capture());
        SavedJob saved = captor.getValue();
        assertEquals("user", saved.getUserId());
        assertEquals(job, saved.getJob());
        assertNotNull(saved.getSavedAt());
    }

    @Test
    void saveJobForUserThrowsWhenJobMissing() {
        when(jobRepository.findById(42L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> savedJobService.saveJobForUser(42L, "user"));
        assertTrue(ex.getMessage().contains("42"));
        verify(savedJobRepository, never()).save(any());
    }

    @Test
    void getSavedJobsForUserReturnsOrderedList() {
        Job job1 = Job.builder().id(1L).title("Role A").build();
        Job job2 = Job.builder().id(2L).title("Role B").build();
        when(savedJobRepository.findJobsByUserIdOrderBySavedAtDesc("user"))
                .thenReturn(List.of(job1, job2));

        List<Job> result = savedJobService.getSavedJobsForUser("user");

        assertEquals(2, result.size());
        assertEquals("Role A", result.get(0).getTitle());
        assertEquals("Role B", result.get(1).getTitle());
    }

    @Test
    void removeSavedJobDeletesWhenPresent() {
        SavedJob savedJob = SavedJob.builder()
                .id(99L)
                .userId("user")
                .job(Job.builder().id(1L).build())
                .savedAt(LocalDateTime.now())
                .build();
        when(savedJobRepository.findByUserIdAndJobId("user", 1L)).thenReturn(Optional.of(savedJob));

        savedJobService.removeSavedJob(1L, "user");

        verify(savedJobRepository).deleteByUserIdAndJobId("user", 1L);
    }
}
