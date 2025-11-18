package com.eduapp.backend.exception;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

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

}
