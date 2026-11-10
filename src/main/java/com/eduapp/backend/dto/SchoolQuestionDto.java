package com.eduapp.backend.dto;

import com.eduapp.backend.media.SignedUrlSerializer;
import com.eduapp.backend.model.Question;
import com.eduapp.backend.model.QuestionOption;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/** Teacher-facing view of a school question (includes the model answer, both images + MCQ options). */
public class SchoolQuestionDto {
    private Long id;
    private String text;
    private String type;
    private Integer marks;
    private String correctAnswerText;
    private String imageUrl;
    // The model answer is the marking scheme: serve it as a short-lived signed URL (never public),
    // and it is never placed on the student-facing StudentQuestionDto at all.
    @JsonSerialize(using = SignedUrlSerializer.class)
    private String modelAnswerImageUrl;
    private List<SchoolMcqOptionDto> options;

    public static SchoolQuestionDto from(Question q, List<QuestionOption> options) {
        SchoolQuestionDto d = new SchoolQuestionDto();
        d.id = q.getId();
        d.text = q.getText();
        d.type = q.getType() != null ? q.getType().name() : null;
        d.marks = q.getMarks();
        d.correctAnswerText = q.getCorrectAnswerText();
        d.imageUrl = q.getImageUrl();
        d.modelAnswerImageUrl = q.getModelAnswerImageUrl();
        d.options = options == null ? List.of() : options.stream().map(SchoolMcqOptionDto::from).toList();
        return d;
    }

    public Long getId() { return id; }
    public String getText() { return text; }
    public String getType() { return type; }
    public Integer getMarks() { return marks; }
    public String getCorrectAnswerText() { return correctAnswerText; }
    public String getImageUrl() { return imageUrl; }
    public String getModelAnswerImageUrl() { return modelAnswerImageUrl; }
    public List<SchoolMcqOptionDto> getOptions() { return options; }
}
