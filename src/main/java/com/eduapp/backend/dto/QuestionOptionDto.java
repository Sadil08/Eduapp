package com.eduapp.backend.dto;

public class QuestionOptionDto {
    private Long id;
    private String keyLabel;
    private String text;
    private Long questionId;

    public QuestionOptionDto() {}

    public QuestionOptionDto(Long id, String keyLabel, String text, Long questionId) {
        this.id = id;
        this.keyLabel = keyLabel;
        this.text = text;
        this.questionId = questionId;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public String getKeyLabel() { return keyLabel; }
    public String getText() { return text; }
    public Long getQuestionId() { return questionId; }

    public void setId(Long id) { this.id = id; }
    public void setKeyLabel(String keyLabel) { this.keyLabel = keyLabel; }
    public void setText(String text) { this.text = text; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
}
