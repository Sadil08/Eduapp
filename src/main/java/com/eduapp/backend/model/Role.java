package com.eduapp.backend.model;

public enum Role {
    // Consumer (global) tier
    ADMIN,
    STUDENT,
    // School (institutional) tier — see PRODUCT_BUSINESS_PLAN.md §5.1.
    // Authorities are derived automatically as ROLE_<name> in UserService.loadUserByUsername.
    SCHOOL_ADMIN,
    TEACHER,
    SCHOOL_STUDENT
}
