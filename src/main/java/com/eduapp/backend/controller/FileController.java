package com.eduapp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Controller for serving uploaded files
 */
@RestController
@RequestMapping("/api/files")
public class FileController {

    private static final Logger logger = LoggerFactory.getLogger(FileController.class);
    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/{category}/{filename:.+}")
    public ResponseEntity<Resource> serveFile(
        @PathVariable String category,
        @PathVariable String filename
    ) {
        try {
            Path filePath = Paths.get(UPLOAD_DIR + category + "/" + filename);
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() && resource.isReadable()) {
                // Determine content type
                String contentType = "application/octet-stream";
                if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                    contentType = "image/jpeg";
                } else if (filename.endsWith(".png")) {
                    contentType = "image/png";
                } else if (filename.endsWith(".heic")) {
                    contentType = "image/heic";
                }

                return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(resource);
            } else {
                logger.warn("File not found or not readable: {}", filePath);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error serving file: {}", e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
