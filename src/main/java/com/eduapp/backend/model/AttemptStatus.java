package com.eduapp.backend.model;

public enum AttemptStatus {
    IN_PROGRESS,
    SUBMITTED,
    GRADED,
    ABANDONED  // When user retries paper, old IN_PROGRESS attempt is marked as abandoned
}
