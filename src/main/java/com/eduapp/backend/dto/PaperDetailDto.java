package com.eduapp.backend.dto;

import java.util.List;

public class PaperDetailDto extends PaperSummaryDto {
    private List<QuestionDto> questions;

    public PaperDetailDto() {
        super();
    }

    public List<QuestionDto> getQuestions() { return questions; }
    public void setQuestions(List<QuestionDto> questions) { this.questions = questions; }
}
