package com.eduapp.backend.exception;

/**
 * Exception thrown when extraction limit is exceeded for a question.
 * Returns HTTP 403 Forbidden to client.
 */
public class ExtractionLimitExceededException extends RuntimeException {

    private final int extractionsUsed;
    private final int extractionsMax;

    public ExtractionLimitExceededException(String message, int extractionsUsed, int extractionsMax) {
        super(message);
        this.extractionsUsed = extractionsUsed;
        this.extractionsMax = extractionsMax;
    }

    public ExtractionLimitExceededException(String message) {
        super(message);
        this.extractionsUsed = 2;
        this.extractionsMax = 2;
    }

    public int getExtractionsUsed() {
        return extractionsUsed;
    }

    public int getExtractionsMax() {
        return extractionsMax;
    }
}
