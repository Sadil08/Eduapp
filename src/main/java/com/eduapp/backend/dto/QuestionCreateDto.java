package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionType;
import java.util.List;

/**
 * DTO for creating or updating questions.
 * Used by admin to manage question content.
 */
public class QuestionCreateDto {
    private String text;
    private QuestionType type;
    private String correctAnswerText;
    private Integer marks;
    private List<QuestionOptionDto> options;

    public QuestionCreateDto() {
    }

    public QuestionCreateDto(String text, QuestionType type, String correctAnswerText,
            Integer marks, List<QuestionOptionDto> options) {
        this.text = text;
        this.type = type;
        this.correctAnswerText = correctAnswerText;
        this.marks = marks;
        this.options = options;
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

    // New fields for image support
    private Long lessonId; // For context-primed AI extraction
    private String imageUrl;
    private String modelAnswerImageUrl;
    private String extractedText;
    private Boolean requiresImageDisplay;
    private Boolean hideQuestionText;
    private Boolean allowImageAnswer;
    private String answerTypeHint;

    public Long getLessonId() {
        return lessonId;
    }

    public void setLessonId(Long lessonId) {
        this.lessonId = lessonId;
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

    public Boolean getHideQuestionText() {
        return hideQuestionText;
    }

    public void setHideQuestionText(Boolean hideQuestionText) {
        this.hideQuestionText = hideQuestionText;
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
}
