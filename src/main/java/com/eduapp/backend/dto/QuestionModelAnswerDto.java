package com.eduapp.backend.dto;

import com.eduapp.backend.media.SignedUrlSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class QuestionModelAnswerDto {
    private Long id;
    private Long questionId;
    private String answerText;
    @JsonSerialize(using = SignedUrlSerializer.class)
    private String imageUrl;
    private String extractedText;

    public QuestionModelAnswerDto() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
