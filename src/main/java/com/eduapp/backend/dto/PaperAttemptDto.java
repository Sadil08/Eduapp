package com.eduapp.backend.dto;

import java.util.List;

public class PaperAttemptDto extends PaperSummaryDto {
    private List<QuestionAttemptDto> questions;

    public PaperAttemptDto() {
        super();
    }

    public List<QuestionAttemptDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionAttemptDto> questions) {
        this.questions = questions;
    }
}
