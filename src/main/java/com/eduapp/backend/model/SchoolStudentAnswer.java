package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * A single answer within a {@link SchoolPaperAttempt}. Kept separate from the consumer
 * {@code StudentAnswer} so the school tier stays tenant-clean, but carries the same
 * fields the marking pipeline reads/writes (answer text, marks awarded, AI feedback).
 */
@Entity
@Table(name = "school_student_answers",
        uniqueConstraints = @UniqueConstraint(name = "uk_school_answer_attempt_question",
                columnNames = {"attempt_id", "question_id"}))
public class SchoolStudentAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "attempt_id", nullable = false)
    private Long attemptId;

    @Column(name = "question_id", nullable = false)
    private Long questionId;

    @Column(name = "answer_text", columnDefinition = "TEXT")
    private String answerText;

    // MCQ: the option the student selected (id of a QuestionOption). Null for essay/extended answers.
    @Column(name = "selected_option_id")
    private Long selectedOptionId;

    @Column(name = "marks_awarded")
    private Integer marksAwarded;

    @Column(name = "ai_feedback", columnDefinition = "TEXT")
    private String aiFeedback;

    // Image-answer support (handwritten responses): the stored image + its AI-extracted text.
    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt = LocalDateTime.now();

    public SchoolStudentAnswer() {
    }

    public SchoolStudentAnswer(Long attemptId, Long questionId, String answerText) {
        this.attemptId = attemptId;
        this.questionId = questionId;
        this.answerText = answerText;
    }

    public Long getId() {
        return id;
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getAnswerText() {
        return answerText;
    }

    public void setAnswerText(String answerText) {
        this.answerText = answerText;
    }

    public Long getSelectedOptionId() {
        return selectedOptionId;
    }

    public void setSelectedOptionId(Long selectedOptionId) {
        this.selectedOptionId = selectedOptionId;
    }

    public Integer getMarksAwarded() {
        return marksAwarded;
    }

    public void setMarksAwarded(Integer marksAwarded) {
        this.marksAwarded = marksAwarded;
    }

    public String getAiFeedback() {
        return aiFeedback;
    }

    public void setAiFeedback(String aiFeedback) {
        this.aiFeedback = aiFeedback;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }
}
