package com.eduapp.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

/**
 * Configuration for Supabase Storage via S3-compatible API.
 */
@Configuration
public class SupabaseStorageConfig {

    private static final Logger logger = LoggerFactory.getLogger(SupabaseStorageConfig.class);

    @Value("${supabase.storage.endpoint}")
    private String endpoint;

    @Value("${supabase.storage.access-key}")
    private String accessKey;

    @Value("${supabase.storage.secret-key}")
    private String secretKey;

    @Bean
    public S3Client supabaseS3Client() {
        if ("not-configured".equals(accessKey) || "not-configured".equals(secretKey)) {
            logger.warn("⚠️  Supabase Storage credentials not configured! "
                    + "Set SUPABASE_S3_ACCESS_KEY and SUPABASE_S3_SECRET_KEY in .env. "
                    + "File uploads will fail until credentials are provided.");
        }

        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .region(Region.of("auto"))
                .forcePathStyle(true)
                .build();
    }
}
