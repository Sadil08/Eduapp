package com.eduapp.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final Logger logger = LoggerFactory.getLogger(FileStorageService.class);

    /** Default lifetime for signed media links (WP-A0.3). */
    public static final Duration SIGNED_URL_TTL = Duration.ofMinutes(10);

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final String bucketName;
    private final String publicUrl;

    public FileStorageService(
            S3Client s3Client,
            S3Presigner s3Presigner,
            @Value("${supabase.storage.bucket-name}") String bucketName,
            @Value("${supabase.storage.public-url}") String publicUrl) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
        this.bucketName = bucketName;
        this.publicUrl = publicUrl;
    }

    /**
     * WP-A0.3: short-lived signed GET URL for a stored object. Accepts either a raw object
     * key ("category/file.png") or a full stored URL (legacy records hold the public URL).
     * Enables a PRIVATE bucket served via expiring links behind auth.
     */
    public String generateSignedUrl(String keyOrUrl, Duration ttl) {
        String objectKey = toObjectKey(keyOrUrl);
        GetObjectRequest get = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();
        GetObjectPresignRequest presign = GetObjectPresignRequest.builder()
                .signatureDuration(ttl != null ? ttl : SIGNED_URL_TTL)
                .getObjectRequest(get)
                .build();
        return s3Presigner.presignGetObject(presign).url().toString();
    }

    /** Normalises a stored value (full public URL or bare key) to an S3 object key. */
    public String toObjectKey(String keyOrUrl) {
        if (keyOrUrl == null) {
            return null;
        }
        if (keyOrUrl.startsWith(publicUrl)) {
            return keyOrUrl.substring(publicUrl.length() + 1); // strip "publicUrl/"
        }
        // Legacy /api/files/<category>/<file> or already-bare key.
        if (keyOrUrl.startsWith("/api/files/")) {
            return keyOrUrl.substring("/api/files/".length());
        }
        return keyOrUrl;
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

        // SEC-7: content-type is client-supplied and spoofable. Verify the actual bytes carry a
        // real image signature, so a renamed script/polyglot labelled image/png is rejected.
        byte[] bytes = file.getBytes();
        if (!looksLikeAllowedImage(bytes)) {
            throw new IllegalArgumentException("File content is not a valid image");
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

        s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));

        // Return full public URL
        String fileUrl = publicUrl + "/" + objectKey;
        logger.info("Successfully stored file to Supabase: {}", fileUrl);

        return fileUrl;
    }

    /**
     * Store raw bytes (e.g., decoded base64 images) to Supabase Storage.
     *
     * @param bytes       The raw bytes to store
     * @param contentType MIME type (e.g., "image/png")
     * @param category    Category folder: 'questions', 'model-answers', etc.
     * @return The full public URL to access the stored file
     */
    public String storeBytes(byte[] bytes, String contentType, String category) {
        if (bytes == null || bytes.length == 0) {
            throw new IllegalArgumentException("Cannot store empty bytes");
        }

        String extension = ".png";
        if ("image/jpeg".equals(contentType))
            extension = ".jpg";
        else if ("image/webp".equals(contentType))
            extension = ".webp";

        String filename = UUID.randomUUID().toString() + extension;
        String objectKey = category + "/" + filename;

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        s3Client.putObject(putRequest, RequestBody.fromBytes(bytes));

        String fileUrl = publicUrl + "/" + objectKey;
        logger.info("Successfully stored bytes to Supabase: {} ({} bytes)", fileUrl, bytes.length);

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

    /**
     * SEC-7: true if the leading bytes match a supported image signature (JPEG/PNG/GIF/WEBP/
     * HEIC-HEIF) — content-based validation independent of the client's declared content-type.
     */
    static boolean looksLikeAllowedImage(byte[] b) {
        if (b == null || b.length < 12) {
            return false;
        }
        // JPEG: FF D8 FF
        if ((b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return true;
        }
        // PNG: 89 50 4E 47 0D 0A 1A 0A
        if ((b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G'
                && (b[4] & 0xFF) == 0x0D && (b[5] & 0xFF) == 0x0A) {
            return true;
        }
        // GIF: "GIF8"
        if (b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') {
            return true;
        }
        // WEBP: "RIFF"...."WEBP"
        if (b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return true;
        }
        // HEIC/HEIF: bytes 4..7 == "ftyp" with a heic/heif/mif1 brand at 8..11
        if (b[4] == 'f' && b[5] == 't' && b[6] == 'y' && b[7] == 'p') {
            String brand = new String(new char[]{(char) b[8], (char) b[9], (char) b[10], (char) b[11]});
            return brand.startsWith("hei") || brand.startsWith("mif") || brand.startsWith("hev")
                    || brand.startsWith("msf") || brand.startsWith("avif");
        }
        return false;
    }
}
