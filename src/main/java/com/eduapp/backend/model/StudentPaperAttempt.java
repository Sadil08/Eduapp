package com.eduapp.backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "student_paper_attempts")
public class StudentPaperAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnore
    private User student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @Column
    private Integer attemptNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_bundle_id")
    private PaperBundle originBundle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "origin_custom_bundle_id")
    private CustomBundle originCustomBundle;

    @Column
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime completedAt;

    @Enumerated(EnumType.STRING)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column
    private Integer timeTakenMinutes;

    @Column
    private Boolean optedIn = false;

    // --- New fields for extraction tracking and analysis resubmission ---

    @Column(name = "elapsed_time_seconds")
    private Integer elapsedTimeSeconds;

    @Column(name = "analysis_completed", nullable = false)
    private Boolean analysisCompleted = false;

    @Column(name = "analysis_attempted", nullable = false)
    private Boolean analysisAttempted = false;

    @Column(name = "analysis_error", length = 1000)
    private String analysisError;

    @Column(name = "submission_count", nullable = false)
    private Integer submissionCount = 0;

    @Column(name = "last_submission_time")
    private LocalDateTime lastSubmissionTime;

    // Relationship to answers (existing)
    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentAnswer> answers = new ArrayList<>();

    // Relationship to extraction tracking
    @OneToMany(mappedBy = "studentPaperAttempt", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionExtractionTracking> extractionTrackings = new ArrayList<>();

    public StudentPaperAttempt() {
    }

    public StudentPaperAttempt(User student, Paper paper, Integer attemptNumber, LocalDateTime startedAt) {
        this.student = student;
        this.paper = paper;
        this.attemptNumber = attemptNumber;
        this.startedAt = startedAt;
        this.status = AttemptStatus.IN_PROGRESS;
    }

    // getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getStudent() {
        return student;
    }

    public void setStudent(User student) {
        this.student = student;
    }

    public Paper getPaper() {
        return paper;
    }

    public void setPaper(Paper paper) {
        this.paper = paper;
    }

    public Integer getAttemptNumber() {
        return attemptNumber;
    }

    public void setAttemptNumber(Integer attemptNumber) {
        this.attemptNumber = attemptNumber;
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

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public Integer getTimeTakenMinutes() {
        return timeTakenMinutes;
    }

    public void setTimeTakenMinutes(Integer timeTakenMinutes) {
        this.timeTakenMinutes = timeTakenMinutes;
    }

    public Boolean getOptedIn() {
        return optedIn;
    }

    public void setOptedIn(Boolean optedIn) {
        this.optedIn = optedIn;
    }

    public List<StudentAnswer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<StudentAnswer> answers) {
        this.answers = answers;
    }

    public Integer getElapsedTimeSeconds() {
        return elapsedTimeSeconds;
    }

    public void setElapsedTimeSeconds(Integer elapsedTimeSeconds) {
        this.elapsedTimeSeconds = elapsedTimeSeconds;
    }

    public Boolean getAnalysisCompleted() {
        return analysisCompleted;
    }

    public void setAnalysisCompleted(Boolean analysisCompleted) {
        this.analysisCompleted = analysisCompleted;
    }

    public Boolean getAnalysisAttempted() {
        return analysisAttempted;
    }

    public void setAnalysisAttempted(Boolean analysisAttempted) {
        this.analysisAttempted = analysisAttempted;
    }

    public String getAnalysisError() {
        return analysisError;
    }

    public void setAnalysisError(String analysisError) {
        this.analysisError = analysisError;
    }

    public Integer getSubmissionCount() {
        return submissionCount;
    }

    public void setSubmissionCount(Integer submissionCount) {
        this.submissionCount = submissionCount;
    }

    public LocalDateTime getLastSubmissionTime() {
        return lastSubmissionTime;
    }

    public void setLastSubmissionTime(LocalDateTime lastSubmissionTime) {
        this.lastSubmissionTime = lastSubmissionTime;
    }

    public List<QuestionExtractionTracking> getExtractionTrackings() {
        return extractionTrackings;
    }

    public void setExtractionTrackings(List<QuestionExtractionTracking> extractionTrackings) {
        this.extractionTrackings = extractionTrackings;
    }

    public PaperBundle getOriginBundle() {
        return originBundle;
    }

    public void setOriginBundle(PaperBundle originBundle) {
        this.originBundle = originBundle;
    }

    public CustomBundle getOriginCustomBundle() {
        return originCustomBundle;
    }

    public void setOriginCustomBundle(CustomBundle originCustomBundle) {
        this.originCustomBundle = originCustomBundle;
    }
}
