package com.eduapp.backend.media;

import com.eduapp.backend.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * WP-A0.3: turns a stored image reference (full Supabase URL or bare object key) into a
 * short-lived signed URL at response-serialization time, so the bucket can be PRIVATE.
 *
 * Holds a static reference to {@link FileStorageService} so the lightweight Jackson
 * serializer ({@link SignedUrlSerializer}) can sign without per-instance Spring wiring.
 * Fails open (returns the original value) if signing is impossible, so serialization is
 * never broken by a storage hiccup.
 */
@Component
public class MediaUrlSigner {

    private static final Logger logger = LoggerFactory.getLogger(MediaUrlSigner.class);
    private static FileStorageService storage;

    public MediaUrlSigner(FileStorageService storage) {
        MediaUrlSigner.storage = storage;
    }

    public static String sign(String storedValue) {
        if (storedValue == null || storedValue.isBlank() || storage == null) {
            return storedValue;
        }
        try {
            return storage.generateSignedUrl(storedValue, FileStorageService.SIGNED_URL_TTL);
        } catch (Exception e) {
            logger.warn("Failed to sign media URL; returning stored value. {}", e.getMessage());
            return storedValue;
        }
    }
}
