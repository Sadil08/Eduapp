package com.eduapp.backend.dto;

import com.eduapp.backend.model.SchoolPaperAttempt;

/**
 * Student-facing result. Marks are present ONLY when the teacher has released results
 * for the paper; otherwise {@code released} is false and the marks are withheld.
 */
public class StudentAttemptResultDto {
    private Long attemptId;
    private String status;
    private boolean released;
    private Integer mark; // effective mark, only when released

    public static StudentAttemptResultDto of(SchoolPaperAttempt a, boolean released) {
        StudentAttemptResultDto d = new StudentAttemptResultDto();
        d.attemptId = a.getId();
        d.status = a.getStatus().name();
        d.released = released;
        d.mark = released ? a.getEffectiveMark() : null;
        return d;
    }

    public Long getAttemptId() { return attemptId; }
    public String getStatus() { return status; }
    public boolean isReleased() { return released; }
    public Integer getMark() { return mark; }
}
