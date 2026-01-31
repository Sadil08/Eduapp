package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Tracks image extraction attempts per question per student attempt.
 * Enforces extraction limits (default: 2 per question) to prevent API abuse.
 * Persists across browser sessions and device switches.
 */
@Entity
@Table(name = "question_extraction_tracking", uniqueConstraints = @UniqueConstraint(columnNames = {
        "student_paper_attempt_id", "question_id" }))
public class QuestionExtractionTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_paper_attempt_id", nullable = false)
    private StudentPaperAttempt studentPaperAttempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "extraction_count", nullable = false)
    private Integer extractionCount = 0;

    @Column(name = "max_extractions", nullable = false)
    private Integer maxExtractions = 2;

    @Column(name = "last_extraction_time")
    private LocalDateTime lastExtractionTime;

    // Constructors
    public QuestionExtractionTracking() {
    }

    public QuestionExtractionTracking(StudentPaperAttempt studentPaperAttempt, Question question) {
        this.studentPaperAttempt = studentPaperAttempt;
        this.question = question;
        this.extractionCount = 0;
        this.maxExtractions = 2;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public StudentPaperAttempt getStudentPaperAttempt() {
        return studentPaperAttempt;
    }

    public void setStudentPaperAttempt(StudentPaperAttempt studentPaperAttempt) {
        this.studentPaperAttempt = studentPaperAttempt;
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Integer getExtractionCount() {
        return extractionCount;
    }

    public void setExtractionCount(Integer extractionCount) {
        this.extractionCount = extractionCount;
    }

    public Integer getMaxExtractions() {
        return maxExtractions;
    }

    public void setMaxExtractions(Integer maxExtractions) {
        this.maxExtractions = maxExtractions;
    }

    public LocalDateTime getLastExtractionTime() {
        return lastExtractionTime;
    }

    public void setLastExtractionTime(LocalDateTime lastExtractionTime) {
        this.lastExtractionTime = lastExtractionTime;
    }

    /**
     * Increment extraction count and update last extraction time
     */
    public void incrementCount() {
        this.extractionCount++;
        this.lastExtractionTime = LocalDateTime.now();
    }

    /**
     * Check if more extractions are allowed
     */
    public boolean canExtract() {
        return this.extractionCount < this.maxExtractions;
    }

    /**
     * Get remaining extraction attempts
     */
    public int getRemainingExtractions() {
        return Math.max(0, this.maxExtractions - this.extractionCount);
    }
}
