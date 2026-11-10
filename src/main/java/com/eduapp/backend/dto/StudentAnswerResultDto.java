package com.eduapp.backend.dto;

import com.eduapp.backend.media.SignedUrlSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Per-question result for a student, shown ONLY once the teacher has released results. Reveals
 * the marking scheme (correct answer / model answer) deliberately — it never leaves the server
 * until release. Image URLs are served as short-lived signed URLs.
 */
public class StudentAnswerResultDto {
    private Long questionId;
    private String questionText;
    private String type;
    private Integer maxMarks;
    private Integer marksAwarded;

    private String answerText;            // what the student wrote (or the chosen option label)
    @JsonSerialize(using = SignedUrlSerializer.class)
    private String imageUrl;              // student's uploaded answer image, if any
    private String aiFeedback;

    private String correctAnswerText;     // marking scheme (essay) — released only
    @JsonSerialize(using = SignedUrlSerializer.class)
    private String modelAnswerImageUrl;   // model-answer image (essay) — released only

    private Long selectedOptionId;        // MCQ
    private List<StudentResultOptionDto> options;

    public Long getQuestionId() { return questionId; }
    public void setQuestionId(Long questionId) { this.questionId = questionId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Integer getMaxMarks() { return maxMarks; }
    public void setMaxMarks(Integer maxMarks) { this.maxMarks = maxMarks; }
    public Integer getMarksAwarded() { return marksAwarded; }
    public void setMarksAwarded(Integer marksAwarded) { this.marksAwarded = marksAwarded; }
    public String getAnswerText() { return answerText; }
    public void setAnswerText(String answerText) { this.answerText = answerText; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getAiFeedback() { return aiFeedback; }
    public void setAiFeedback(String aiFeedback) { this.aiFeedback = aiFeedback; }
    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }
    public String getModelAnswerImageUrl() { return modelAnswerImageUrl; }
    public void setModelAnswerImageUrl(String modelAnswerImageUrl) { this.modelAnswerImageUrl = modelAnswerImageUrl; }
    public Long getSelectedOptionId() { return selectedOptionId; }
    public void setSelectedOptionId(Long selectedOptionId) { this.selectedOptionId = selectedOptionId; }
    public List<StudentResultOptionDto> getOptions() { return options; }
    public void setOptions(List<StudentResultOptionDto> options) { this.options = options; }
}
