package com.eduapp.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for student paper attempts with complete results.
 * Used to return attempt details including answers, marks, and AI feedback.
 */
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
    private String overallFeedback; // AI-generated overall feedback
    private Integer totalMarks; // Total marks awarded by AI
    private Integer paperTotalMarks; // Total marks allocated for the paper
    private String videoUrl; // YouTube video URL from the paper

    private String analysisError;
    private Boolean analysisCompleted;
    private Integer submissionCount;
    private Long originBundleId;

    public StudentPaperAttemptDto() {
    }

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStudentId() {
        return studentId;
    }

    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(LocalDateTime startedAt) {
        this.startedAt = startedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public Integer getTimeTakenMinutes() {
        return timeTakenMinutes;
    }

    public void setTimeTakenMinutes(Integer timeTakenMinutes) {
        this.timeTakenMinutes = timeTakenMinutes;
    }

    public List<StudentAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<StudentAnswerDto> answers) {
        this.answers = answers;
    }

    public String getOverallFeedback() {
        return overallFeedback;
    }

    public void setOverallFeedback(String overallFeedback) {
        this.overallFeedback = overallFeedback;
    }

    public Integer getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(Integer totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Integer getPaperTotalMarks() {
        return paperTotalMarks;
    }

    public void setPaperTotalMarks(Integer paperTotalMarks) {
        this.paperTotalMarks = paperTotalMarks;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getAnalysisError() {
        return analysisError;
    }

    public void setAnalysisError(String analysisError) {
        this.analysisError = analysisError;
    }

    public Boolean getAnalysisCompleted() {
        return analysisCompleted;
    }

    public void setAnalysisCompleted(Boolean analysisCompleted) {
        this.analysisCompleted = analysisCompleted;
    }

    public Integer getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
    }

    public Long getOriginBundleId() {
        return originBundleId;
    }

    public void setOriginBundleId(Long originBundleId) {
        this.originBundleId = originBundleId;
    }

    private Long originCustomBundleId;

    public Long getOriginCustomBundleId() {
        return originCustomBundleId;
    }

    public void setOriginCustomBundleId(Long originCustomBundleId) {
        this.originCustomBundleId = originCustomBundleId;
    }
}
