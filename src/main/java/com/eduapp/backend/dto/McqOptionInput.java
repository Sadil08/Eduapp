package com.eduapp.backend.dto;

/** One MCQ option as submitted by a teacher when authoring a question. Order is by list position. */
public class McqOptionInput {
    private String text;
    private boolean correct;

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public boolean isCorrect() { return correct; }
    public void setCorrect(boolean correct) { this.correct = correct; }
}
