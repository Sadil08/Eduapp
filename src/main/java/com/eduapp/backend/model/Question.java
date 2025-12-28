package com.eduapp.backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    @JsonIgnore
    private Paper paper;

    @Column(length = 4000, nullable = false)
    private String text;

    @Column(length = 4000)
    private String imageUrl;

    @Column(name = "model_answer_image_url", length = 4000)
    private String modelAnswerImageUrl;

    @Column(length = 4000)
    private String extractedText;

    @Column(name = "requires_image_display")
    private Boolean requiresImageDisplay = false;

    @Column(name = "allow_image_answer")
    private Boolean allowImageAnswer = true;

    @Column(name = "answer_type_hint", length = 20)
    private String answerTypeHint = "essay";

    @Column(name = "hide_question_text")
    private Boolean hideQuestionText = false;

    @Column(name = "extraction_confidence")
    private Float extractionConfidence;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(length = 4000)
    private String correctAnswerText;

    @Column
    private Integer marks;

    @ManyToOne(fetch = FetchType.EAGER) // Eagerly load lesson for context
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne
    @JoinColumn(name = "created_by")
    @JsonIgnore
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();

    public Question() {
    }

    public Question(Paper paper, String text, QuestionType type, String correctAnswerText) {
        this.paper = paper;
        this.text = text;
        this.type = type;
        this.correctAnswerText = correctAnswerText;
    }

    // getters/setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Paper getPaper() {
        return paper;
    }

    public void setPaper(Paper paper) {
        this.paper = paper;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getModelAnswerImageUrl() {
        return modelAnswerImageUrl;
    }

    public void setModelAnswerImageUrl(String modelAnswerImageUrl) {
        this.modelAnswerImageUrl = modelAnswerImageUrl;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }

    public Boolean getRequiresImageDisplay() {
        return requiresImageDisplay;
    }

    public void setRequiresImageDisplay(Boolean requiresImageDisplay) {
        this.requiresImageDisplay = requiresImageDisplay;
    }

    public Boolean getAllowImageAnswer() {
        return allowImageAnswer;
    }

    public void setAllowImageAnswer(Boolean allowImageAnswer) {
        this.allowImageAnswer = allowImageAnswer;
    }

    public String getAnswerTypeHint() {
        return answerTypeHint;
    }

    public void setAnswerTypeHint(String answerTypeHint) {
        this.answerTypeHint = answerTypeHint;
    }

    public Boolean getHideQuestionText() {
        return hideQuestionText;
    }

    public void setHideQuestionText(Boolean hideQuestionText) {
        this.hideQuestionText = hideQuestionText;
    }

    public Float getExtractionConfidence() {
        return extractionConfidence;
    }

    public void setExtractionConfidence(Float extractionConfidence) {
        this.extractionConfidence = extractionConfidence;
    }

    public QuestionType getType() {
        return type;
    }

    public void setType(QuestionType type) {
        this.type = type;
    }

    public String getCorrectAnswerText() {
        return correctAnswerText;
    }

    public void setCorrectAnswerText(String correctAnswerText) {
        this.correctAnswerText = correctAnswerText;
    }

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<QuestionOption> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOption> options) {
        this.options = options;
    }
}
