package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionType;
import java.util.List;

public class QuestionDto {
    private Long id;
    private Long paperId;
    private String text;
    private QuestionType type;
    private String correctAnswerText;
    private String imageUrl;
    private String modelAnswerImageUrl;
    private String extractedText;
    private Boolean requiresImageDisplay;
    private Boolean allowImageAnswer;
    private String answerTypeHint;
    private Boolean hideQuestionText;
    private Integer marks;
    private List<QuestionOptionDto> options;

    public QuestionDto() {
    }

    public QuestionDto(Long id, Long paperId, String text, QuestionType type, String correctAnswerText,
            List<QuestionOptionDto> options) {
        this.id = id;
        this.paperId = paperId;
        this.text = text;
        this.type = type;
        this.correctAnswerText = correctAnswerText;
        this.options = options;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPaperId() {
        return paperId;
    }

    public void setPaperId(Long paperId) {
        this.paperId = paperId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public List<QuestionOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionDto> options) {
        this.options = options;
    }
}
