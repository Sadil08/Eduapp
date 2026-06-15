package com.eduapp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

// NOTE: @EnableAsync lives solely on AsyncConfig (the single owner of async configuration).
// It was previously duplicated here — removed to resolve the ambiguity (specs/ambiguities.md).
// @EnableScheduling powers the durable MarkingSweeper (WP-A1).
@SpringBootApplication
@EnableCaching
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
