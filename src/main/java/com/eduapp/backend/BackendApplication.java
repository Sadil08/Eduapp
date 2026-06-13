package com.eduapp.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

// NOTE: @EnableAsync lives solely on AsyncConfig (the single owner of async configuration).
// It was previously duplicated here — removed to resolve the ambiguity (specs/ambiguities.md).
@SpringBootApplication
@EnableCaching
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

}
