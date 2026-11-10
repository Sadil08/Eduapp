package com.eduapp.backend.service;

/**
 * SCALE-MARK-1: thrown by {@link AiCallGuard} when an AI call is fast-failed because the circuit
 * is open or the bulkhead is full. Distinct from a real upstream error so callers can treat it as
 * a transient, retryable condition (the marking sweeper will re-enqueue the attempt).
 */
public class AiUnavailableException extends RuntimeException {
    public AiUnavailableException(String message) {
        super(message);
    }
}
