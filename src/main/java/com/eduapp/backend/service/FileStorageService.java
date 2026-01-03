package com.eduapp.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    @Value("${supabase.s3.endpoint}")
    private String s3Endpoint;

    @Value("${supabase.s3.region:us-east-1}")
    private String s3Region;

    @Value("${supabase.s3.access-key}")
    private String accessKey;

    @Value("${supabase.s3.secret-key}")
    private String secretKey;

    @Value("${supabase.s3.bucket-name}")
    private String bucketName;

    // Supabase project ID for constructing public URLs if needed, or derived from
    // endpoint
    @Value("${supabase.project.url:}")
    private String projectUrl;

    private S3Client s3Client;

    public FileStorageService() {
        // Client initialized in @PostConstruct or lazily to allow property injection
    }

    @jakarta.annotation.PostConstruct
    public void init() {
        try {
            this.s3Client = S3Client.builder()
                    .region(Region.of(s3Region))
                    .endpointOverride(URI.create(s3Endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKey, secretKey)))
                    .forcePathStyle(true) // Required for Supabase/MinIO
                    .build();
            logger.info("S3 Client initialized for endpoint: {}", s3Endpoint);
        } catch (Exception e) {
            logger.error("Failed to initialize S3 Client: {}", e.getMessage());
        }
    }

    /**
     * Store an uploaded file in the appropriate category folder in S3 bucket
     * 
     * @param file     The multipart file to store
     * @param category Category folder: 'questions', 'model-answers', or
     *                 'student-answers'
     * @return The Public URL to access the stored file
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
        String s3Key = category + "/" + filename;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(contentType)
                    // .acl(ObjectCannedACL.PUBLIC_READ) // Supabase handles permissions via
                    // policies, usually not ACL
                    .build();

            s3Client.putObject(putObjectRequest,
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            logger.info("Successfully uploaded file to S3: {}", s3Key);

            // Construct Public URL
            String publicBaseUrl = s3Endpoint.replace("/s3", "/object/public");
            return publicBaseUrl + "/" + bucketName + "/" + s3Key;

        } catch (Exception e) {
            logger.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to storage provider", e);
        }
    }

    /**
     * Delete a file given its URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty())
            return;

        try {
            String splitToken = "/" + bucketName + "/";
            int index = fileUrl.indexOf(splitToken);
            if (index != -1) {
                String key = fileUrl.substring(index + splitToken.length());

                DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                        .bucket(bucketName)
                        .key(key)
                        .build();

                s3Client.deleteObject(deleteRequest);
                logger.info("Deleted file from S3: {}", key);
            }
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
