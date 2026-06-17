package com.eduapp.backend.model;

/** Status of a student's enrolment in a class. */
public enum SchoolEnrolmentStatus {
    /** BT-9: self-requested join awaiting a teacher's approval. No class access yet. */
    PENDING,
    /** Approved by a teacher, or teacher/admin direct-enrolled. Full class access. */
    ACTIVE,
    /** Request declined by a teacher. */
    REJECTED,
    /** Previously active, later removed from the class. */
    REMOVED
}
