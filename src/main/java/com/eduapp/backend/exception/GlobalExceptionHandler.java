package com.eduapp.backend.exception;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.HashMap;
import java.util.Map;

// Scoped to our API controllers so it does NOT intercept framework/actuator endpoints
// (the catch-all previously turned /actuator/prometheus into a 500 "Something went wrong").
@RestControllerAdvice(basePackages = "com.eduapp.backend.controller")
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    // N-S8: intended, safe-to-show client errors return their message.
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage(), "code", 400));
    }

    // N-S9: structured field errors from @Valid — no internal detail.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fields = new HashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(fe -> fields.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of(
                "error", "Validation failed", "code", 400, "fields", fields));
    }

    // SEC-IDOR: honour an explicit ResponseStatusException (e.g. the 404 thrown by
    // AttemptAccessGuard). ResponseStatusException is a RuntimeException, so without this more-
    // specific handler the broad handler below would turn every such denial into a 400 — masking
    // the intended status (and, for the IDOR guard, the deliberate 404 no-enumeration response).
    @ExceptionHandler(org.springframework.web.server.ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            org.springframework.web.server.ResponseStatusException ex) {
        String reason = ex.getReason() != null ? ex.getReason() : "Error";
        return ResponseEntity.status(ex.getStatusCode())
                .body(Map.of("error", reason, "code", ex.getStatusCode().value()));
    }

    // N-S8: any OTHER RuntimeException is treated as unexpected — generic message to the
    // client, real detail (which may contain internal/third-party info) logged server-side.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handle(RuntimeException ex) {
        logger.warn("Unhandled RuntimeException at API boundary", ex);
        return ResponseEntity.badRequest().body(Map.of("error", "Request could not be processed", "code", 400));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        logger.error("Unhandled exception at API boundary", ex);
        Map<String, Object> error = Map.of("error", "Something went wrong", "code", 500);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleJsonParseError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidEx && invalidEx.getTargetType().isEnum()) {
            Map<String, Object> error = Map.of("error", "Invalid value. Must be one of: " +
                    java.util.Arrays.toString(invalidEx.getTargetType().getEnumConstants()), "code", 400);
            return ResponseEntity.badRequest().body(error);
        }
        // N-S8: don't echo the raw parser message (can contain payload/internal detail).
        return ResponseEntity.badRequest().body(Map.of("error", "Malformed request body", "code", 400));
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
