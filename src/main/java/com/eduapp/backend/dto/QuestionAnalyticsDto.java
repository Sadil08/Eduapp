package com.eduapp.backend.dto;

import java.io.Serializable;

/** Per-question cohort performance (which questions the class struggled with). */
public class QuestionAnalyticsDto implements Serializable {
    private Long questionId;
    private String text;
    private long answeredCount;
    private Double averageMarks;
    private Integer maxMarks;
    private Double correctRate; // fraction scoring full marks (most meaningful for MCQ)

    public QuestionAnalyticsDto() {
    }

    public QuestionAnalyticsDto(Long questionId, String text, long answeredCount,
            Double averageMarks, Integer maxMarks, Double correctRate) {
        this.questionId = questionId;
        this.text = text;
        this.answeredCount = answeredCount;
        this.averageMarks = averageMarks;
        this.maxMarks = maxMarks;
        this.correctRate = correctRate;
    }

    public Long getQuestionId() { return questionId; }
    public String getText() { return text; }
    public long getAnsweredCount() { return answeredCount; }
    public Double getAverageMarks() { return averageMarks; }
    public Integer getMaxMarks() { return maxMarks; }
    public Double getCorrectRate() { return correctRate; }
}
