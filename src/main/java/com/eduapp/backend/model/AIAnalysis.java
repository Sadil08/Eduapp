package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "ai_analyses")
public class AIAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "answer_id", nullable = false)
    private StudentAnswer answer;

    @Column(length = 2000)
    private String feedback;

    @Column
    private Integer marks;

    @Column(length = 1000)
    private String lessonsToReview;

    private LocalDateTime createdAt = LocalDateTime.now();

    public AIAnalysis() {}

    public AIAnalysis(StudentAnswer answer, String feedback, Integer marks, String lessonsToReview) {
        this.answer = answer;
        this.feedback = feedback;
        this.marks = marks;
        this.lessonsToReview = lessonsToReview;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public StudentAnswer getAnswer() { return answer; }
    public void setAnswer(StudentAnswer answer) { this.answer = answer; }

    public String getFeedback() { return feedback; }
    public void setFeedback(String feedback) { this.feedback = feedback; }

    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }

    public String getLessonsToReview() { return lessonsToReview; }
    public void setLessonsToReview(String lessonsToReview) { this.lessonsToReview = lessonsToReview; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}