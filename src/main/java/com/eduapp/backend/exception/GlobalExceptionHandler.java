package com.eduapp.backend.exception;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // SECURITY: these must be declared BEFORE the broad RuntimeException handler so the
    // more-specific match wins. AccessDeniedException is a RuntimeException, so without
    // this it was being turned into a 400 — masking every method-level @PreAuthorize
    // denial (which should be 403) and leaking the exception message.
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(Map.of("error", "Forbidden", "code", 403));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthentication(AuthenticationException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Unauthorized", "code", 401));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handle(RuntimeException ex) {
        Map<String, Object> error = Map.of("error", ex.getMessage(), "code", 400);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        Map<String, Object> error = Map.of("error", "Something went wrong", "code", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidEx && invalidEx.getTargetType().isEnum()) {
            Map<String, Object> error = Map.of("error", "Invalid role. Must be one of: " +
                    java.util.Arrays.toString(invalidEx.getTargetType().getEnumConstants()), "code", 400);
            return ResponseEntity.badRequest().body(error);
        }
        Map<String, Object> error = Map.of("error", "Invalid JSON format: " + ex.getMessage(), "code", 400);
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(UploadLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleUploadLimitExceeded(UploadLimitExceededException ex) {
        Map<String, Object> error = Map.of(
                "error", ex.getMessage(),
                "code", 400,
                "type", "UPLOAD_LIMIT_EXCEEDED");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

}
