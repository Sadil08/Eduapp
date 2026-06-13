package com.eduapp.backend.model;

/** Lifecycle of a school-tier paper. */
public enum SchoolPaperStatus {
    DRAFT,      // teacher is still adding / reviewing questions
    APPROVED,   // questions reviewed; ready to assign
    ASSIGNED    // assigned to a class with an exam window
}
