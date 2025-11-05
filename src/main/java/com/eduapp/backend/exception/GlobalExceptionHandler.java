package com.eduapp.backend.exception;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.exc.InvalidFormatException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Fallback for normal runtime errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntime(RuntimeException ex) {
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body("❌ " + ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
    }

   // Handles invalid enum values and other JSON format issues
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleJsonParseError(HttpMessageNotReadableException ex) {
        Throwable cause = ex.getCause();
        if (cause instanceof InvalidFormatException invalidEx && invalidEx.getTargetType().isEnum()) {
            return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body("❌ Invalid role. Must be one of: " +
                      java.util.Arrays.toString(invalidEx.getTargetType().getEnumConstants()));
        }
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body("❌ Invalid JSON format: " + ex.getMessage());
    }

}
