package com.eduapp.backend.it;

import com.eduapp.backend.service.FileStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

/** WP-A0.3: stored image references are turned into short-lived signed URLs (private bucket). */
class MediaSigningIT extends AbstractIntegrationTest {

    @Autowired FileStorageService storage;

    @Test
    void generatesExpiringSignedUrlForAnObjectKey() {
        String signed = storage.generateSignedUrl("student-answers/abc.png", Duration.ofMinutes(5));
        assertThat(signed).contains("student-answers/abc.png");
        // AWS SigV4 presigned URLs carry X-Amz-* query parameters (signature + expiry).
        assertThat(signed).containsIgnoringCase("X-Amz-");
    }

    @Test
    void toObjectKey_normalisesLegacyAndBareForms() {
        assertThat(storage.toObjectKey("/api/files/questions/x.png")).isEqualTo("questions/x.png");
        assertThat(storage.toObjectKey("questions/x.png")).isEqualTo("questions/x.png");
        assertThat(storage.toObjectKey(null)).isNull();
    }
}
