package com.eduapp.backend.dto;

import com.eduapp.backend.model.QuestionType;

import java.util.List;

public class AddSchoolQuestionRequest {
    // Not @NotBlank: a question may be image-only (text carried by imageUrl). The service
    // enforces that at least one of text/imageUrl is present.
    private String text;
    private QuestionType type;
    private Integer marks;
    private String correctAnswerText;
    private String imageUrl;            // optional: image of the question itself
    private String modelAnswerImageUrl; // optional: image of the model answer / marking scheme
    private List<McqOptionInput> options; // required (2-10, exactly 1 correct) when type == MCQ

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }
    public Integer getMarks() { return marks; }
    public void setMarks(Integer marks) { this.marks = marks; }
    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getModelAnswerImageUrl() { return modelAnswerImageUrl; }
    public void setModelAnswerImageUrl(String modelAnswerImageUrl) { this.modelAnswerImageUrl = modelAnswerImageUrl; }
    public List<McqOptionInput> getOptions() { return options; }
    public void setOptions(List<McqOptionInput> options) { this.options = options; }
}
