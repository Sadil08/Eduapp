package com.eduapp.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    private final S3Client s3Client;
    private final String bucketName;
    private final String publicUrl;

    public FileStorageService(
            S3Client s3Client,
            @Value("${supabase.storage.bucket-name}") String bucketName,
            @Value("${supabase.storage.public-url}") String publicUrl) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        this.publicUrl = publicUrl;
    }

    /**
     * Store an uploaded file in Supabase Storage under the appropriate category
     * folder.
     *
     * @param file     The multipart file to store
     * @param category Category folder: 'questions', 'model-answers', or
     *                 'student-answers'
     * @return The full public URL to access the stored file
     */
    public String storeFile(MultipartFile file, String category) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }

        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            throw new IllegalArgumentException("Only image files (JPG, PNG, HEIC) are allowed");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("File size exceeds maximum of 5MB");
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";

        // Sanitize extension
        if (extension.length() > 10)
            extension = "";

        String filename = UUID.randomUUID().toString() + extension;
        String objectKey = category + "/" + filename;

        // Upload to Supabase Storage via S3 API
        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(file.getBytes()));

        // Return full public URL
        String fileUrl = publicUrl + "/" + objectKey;
        logger.info("Successfully stored file to Supabase: {}", fileUrl);

        return fileUrl;
    }

    /**
     * Delete a file from Supabase Storage given its URL.
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty())
            return;

        try {
            String objectKey = null;

            if (fileUrl.startsWith(publicUrl)) {
                // New Supabase URL – extract key after the public URL prefix
                objectKey = fileUrl.substring(publicUrl.length() + 1); // +1 for the '/'
            } else if (fileUrl.startsWith("/api/files/")) {
                // Legacy local URL – extract path as the object key
                objectKey = fileUrl.substring("/api/files/".length());
            }

            if (objectKey == null || objectKey.isEmpty()) {
                logger.warn("Cannot determine object key from URL: {}", fileUrl);
                return;
            }

            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.deleteObject(deleteRequest);
            logger.info("Deleted file from Supabase: {}", objectKey);
        } catch (Exception e) {
            logger.error("Failed to delete file: {}", fileUrl, e);
        }
    }

    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg")
                || contentType.equals("image/jpg")
                || contentType.equals("image/png")
                || contentType.equals("image/webp")
                || contentType.equals("image/heic")
                || contentType.equals("image/heif");
    }
}
