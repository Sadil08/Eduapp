package com.eduapp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

/**
 * Backward-compatibility controller for legacy /api/files/ URLs.
 * Redirects to the corresponding Supabase Storage public URL.
 * 
 * New uploads store full Supabase URLs directly, so this controller
 * is only needed for old database records that reference /api/files/... paths.
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);

    @Value("${supabase.storage.public-url}")
    private String publicUrl;

    @GetMapping("/{category}/{filename:.+}")
    public ResponseEntity<Void> serveFile(
            @PathVariable String category,
            @PathVariable String filename) {
        String redirectUrl = publicUrl + "/" + category + "/" + filename;
        logger.info("Redirecting legacy file request to Supabase: {}", redirectUrl);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(redirectUrl))
                .build();
    }
}
