package com.eduapp.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    private static final String UPLOAD_DIR = "uploads/";
    
    public FileStorageService() {
        // Create upload directories if they don't exist
        try {
            Files.createDirectories(Paths.get(UPLOAD_DIR + "questions"));
            Files.createDirectories(Paths.get(UPLOAD_DIR + "model-answers"));
            Files.createDirectories(Paths.get(UPLOAD_DIR + "student-answers"));
        } catch (IOException e) {
            logger.error("Could not create upload directories", e);
        }
    }

    /**
     * Store an uploaded file in the appropriate category folder
     * @param file The multipart file to store
     * @param category Category folder: 'questions', 'model-answers', or 'student-answers'
     * @return The URL/path to access the stored file
     */
    public String storeFile(MultipartFile file, String category) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException("Only image files (JPG, PNG, HEIC) are allowed");
        }

        // Validate file size (5MB max)
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum of 5MB");
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : "";
        
        String filename = UUID.randomUUID().toString() + extension;
        String categoryPath = UPLOAD_DIR + category + "/";
        Path targetLocation = Paths.get(categoryPath + filename);

        // Store file
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
        
        // Return URL that can be served by backend
        String fileUrl = "/api/files/" + category + "/" + filename;
        logger.info("Successfully stored file: {}", fileUrl);
        
        return fileUrl;
    }

    /**
     * Delete a file given its URL
     */
    public void deleteFile(String fileUrl) {
        try {
            if (fileUrl == null || !fileUrl.startsWith("/api/files/")) {
                return;
            }
            
            // Extract path from URL
            String relativePath = fileUrl.substring("/api/files/".length());
            Path filePath = Paths.get(UPLOAD_DIR + relativePath);
            
            Files.deleteIfExists(filePath);
            logger.info("Deleted file: {}", fileUrl);
        } catch (IOException e) {
            logger.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg") 
            || contentType.equals("image/jpg")
            || contentType.equals("image/png")
            || contentType.equals("image/heic")
            || contentType.equals("image/heif");
    }
}
