package com.eduapp.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public class StudentPaperAttemptDto {
    private Long id;
    private Long studentId;
    private Long paperId;
    private Integer attemptNumber;
    private String status;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private Integer timeTakenMinutes;
    private List<StudentAnswerDto> answers;

    public StudentPaperAttemptDto() {}

    public StudentPaperAttemptDto(Long id, Long studentId, Long paperId, Integer attemptNumber, String status,
                                  LocalDateTime startedAt, LocalDateTime completedAt, Integer timeTakenMinutes,
                                  List<StudentAnswerDto> answers) {
        this.id = id;
        this.studentId = studentId;
        this.paperId = paperId;
        this.attemptNumber = attemptNumber;
        this.status = status;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.timeTakenMinutes = timeTakenMinutes;
        this.answers = answers;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

    public Integer getAttemptNumber() { return attemptNumber; }
    public void setAttemptNumber(Integer attemptNumber) { this.attemptNumber = attemptNumber; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getStartedAt() { return startedAt; }
    public void setStartedAt(LocalDateTime startedAt) { this.startedAt = startedAt; }

    public LocalDateTime getCompletedAt() { return completedAt; }
    public void setCompletedAt(LocalDateTime completedAt) { this.completedAt = completedAt; }

    public Integer getTimeTakenMinutes() { return timeTakenMinutes; }
    public void setTimeTakenMinutes(Integer timeTakenMinutes) { this.timeTakenMinutes = timeTakenMinutes; }

    public List<StudentAnswerDto> getAnswers() { return answers; }
    public void setAnswers(List<StudentAnswerDto> answers) { this.answers = answers; }
}
