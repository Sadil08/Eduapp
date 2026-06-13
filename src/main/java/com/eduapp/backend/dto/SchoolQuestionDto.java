package com.eduapp.backend.dto;

import com.eduapp.backend.model.Question;

/** Teacher-facing view of a school question (includes the model answer). */
public class SchoolQuestionDto {
    private Long id;
    private String text;
    private String type;
    private Integer marks;
    private String correctAnswerText;

    public static SchoolQuestionDto from(Question q) {
        SchoolQuestionDto d = new SchoolQuestionDto();
        d.id = q.getId();
        d.text = q.getText();
        d.type = q.getType() != null ? q.getType().name() : null;
        d.marks = q.getMarks();
        d.correctAnswerText = q.getCorrectAnswerText();
        return d;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getType() { return type; }
    public Integer getMarks() { return marks; }
    public String getCorrectAnswerText() { return correctAnswerText; }
}
