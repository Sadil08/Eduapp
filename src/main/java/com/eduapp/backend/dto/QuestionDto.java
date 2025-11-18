package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionType;
import java.util.List;

public class QuestionDto {
    private Long id;
    private Long paperId;
    private String text;
    private QuestionType type;
    private String correctAnswerText;
    private List<QuestionOptionDto> options;

    public QuestionDto() {}

    public QuestionDto(Long id, Long paperId, String text, QuestionType type, String correctAnswerText, List<QuestionOptionDto> options) {
        this.id = id;
        this.paperId = paperId;
        this.text = text;
        this.type = type;
        this.correctAnswerText = correctAnswerText;
        this.options = options;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPaperId() { return paperId; }
    public void setPaperId(Long paperId) { this.paperId = paperId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }

    public List<QuestionOptionDto> getOptions() { return options; }
    public void setOptions(List<QuestionOptionDto> options) { this.options = options; }
}
