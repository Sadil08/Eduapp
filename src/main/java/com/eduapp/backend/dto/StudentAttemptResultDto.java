package com.eduapp.backend.dto;

import com.eduapp.backend.model.SchoolPaperAttempt;

import java.util.List;

/**
 * Student-facing result. Marks AND the per-question breakdown are present ONLY when the teacher
 * has released results for the paper; otherwise {@code released} is false and everything that
 * could leak the marking scheme is withheld.
 */
public class StudentAttemptResultDto {
    private Long attemptId;
    private String status;
    private boolean released;
    private Integer mark;        // effective mark, only when released
    private Integer totalMarks;  // max possible, always present (no leak)
    private List<StudentAnswerResultDto> answers; // per-question, only when released

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
    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }
    public List<StudentAnswerResultDto> getAnswers() { return answers; }
    public void setAnswers(List<StudentAnswerResultDto> answers) { this.answers = answers; }
}
