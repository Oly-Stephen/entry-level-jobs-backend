package com.entry_level_jobs;

import com.entry_level_jobs.config.SecurityProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry Level Jobs Aggregator Application.
 * Spring Boot application for aggregating and filtering entry-level job
 * listings.
 */
@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(SecurityProperties.class)
@Slf4j
public class EntryLevelJobsApplication {

	public static void main(String[] args) {
		log.info("Starting Entry Level Jobs Aggregator Application");
		SpringApplication.run(EntryLevelJobsApplication.class, args);
		log.info("Application started successfully");
	}
}
