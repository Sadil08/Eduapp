package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionType;
import java.util.List;

public class QuestionAttemptDto {
    private Long id;
    private Long paperId;
    private String text;
    private QuestionType type;
    private Integer marks;
    private String imageUrl;
    private Boolean requiresImageDisplay;
    private Boolean hideQuestionText;
    private Boolean allowImageAnswer;
    private String answerTypeHint;
    private List<QuestionOptionDto> options;

    // NEW: Extraction tracking for current attempt
    private Integer extractionsUsed;

    public QuestionAttemptDto() {
    }

    public QuestionAttemptDto(Long id, Long paperId, String text, QuestionType type, List<QuestionOptionDto> options) {
        this.id = id;
        this.paperId = paperId;
        this.text = text;
        this.type = type;
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

    public Integer getMarks() {
        return marks;
    }

    public void setMarks(Integer marks) {
        this.marks = marks;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public List<QuestionOptionDto> getOptions() {
        return options;
    }

    public void setOptions(List<QuestionOptionDto> options) {
        this.options = options;
    }

    public Integer getExtractionsUsed() {
        return extractionsUsed;
    }

    public void setExtractionsUsed(Integer extractionsUsed) {
        this.extractionsUsed = extractionsUsed;
    }
}
