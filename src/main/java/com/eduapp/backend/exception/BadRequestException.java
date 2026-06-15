package com.eduapp.backend.exception;

/**
 * Thrown for *intended*, safe-to-show client errors (e.g. "Email already exists",
 * "Insufficient wallet balance"). Its message IS returned to the client (HTTP 400).
 *
 * <p>N-S8: everything else — raw {@code RuntimeException}, NPEs, third-party/gateway
 * messages — is mapped to a generic message by {@code GlobalExceptionHandler} and only
 * logged server-side, so internal detail never leaks.
 */
public class BadRequestException extends RuntimeException {
    public BadRequestException(String message) {
        super(message);
    }
}
