package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "overall_paper_analyses")
public class OverallPaperAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "attempt_id", nullable = false)
    private StudentPaperAttempt attempt;

    @Column
    private Integer totalMarks;

    @Column(length = 2000)
    private String overallFeedback;

    @Column(length = 1000)
    private String lessonsLacking;

    private LocalDateTime createdAt = LocalDateTime.now();

    public OverallPaperAnalysis() {}

    public OverallPaperAnalysis(StudentPaperAttempt attempt, Integer totalMarks, String overallFeedback, String lessonsLacking) {
        this.attempt = attempt;
        this.totalMarks = totalMarks;
        this.overallFeedback = overallFeedback;
        this.lessonsLacking = lessonsLacking;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentPaperAttempt getAttempt() { return attempt; }
    public void setAttempt(StudentPaperAttempt attempt) { this.attempt = attempt; }

    public Integer getTotalMarks() { return totalMarks; }
    public void setTotalMarks(Integer totalMarks) { this.totalMarks = totalMarks; }

    public String getOverallFeedback() { return overallFeedback; }
    public void setOverallFeedback(String overallFeedback) { this.overallFeedback = overallFeedback; }

    public String getLessonsLacking() { return lessonsLacking; }
    public void setLessonsLacking(String lessonsLacking) { this.lessonsLacking = lessonsLacking; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}