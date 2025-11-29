package com.eduapp.backend.dto;

import java.util.List;

public class PaperSubmissionDto {
    private List<StudentAnswerSubmissionDto> answers;
    private Integer timeTakenMinutes;

    public PaperSubmissionDto() {
    }

    public List<StudentAnswerSubmissionDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<StudentAnswerSubmissionDto> answers) {
        this.answers = answers;
    }

    public Integer getTimeTakenMinutes() {
        return timeTakenMinutes;
    }

    public void setTimeTakenMinutes(Integer timeTakenMinutes) {
        this.timeTakenMinutes = timeTakenMinutes;
    }

    public static class StudentAnswerSubmissionDto {
        private Long questionId;
        private Long selectedOptionId;
        private String answerText;

        public StudentAnswerSubmissionDto() {
        }

        public Long getQuestionId() {
            return questionId;
        }

        public void setQuestionId(Long questionId) {
            this.questionId = questionId;
        }

        public Long getSelectedOptionId() {
            return selectedOptionId;
        }

        public void setSelectedOptionId(Long selectedOptionId) {
            this.selectedOptionId = selectedOptionId;
        }

        public String getAnswerText() {
            return answerText;
        }

        public void setAnswerText(String answerText) {
            this.answerText = answerText;
        }
    }
}
