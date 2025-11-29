package com.eduapp.backend.model;

import jakarta.persistence.*;

@Entity
@Table(name = "question_options")
public class QuestionOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Option key or label (e.g., "A", "B", "C")
    @Column
    private String keyLabel;

    @Column(length = 2000, nullable = false)
    private String text;

    @Column
    private Boolean isCorrect = false;

    @Column
    private Integer orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    public QuestionOption() {}

    public QuestionOption(String keyLabel, String text, Question question) {
        this.keyLabel = keyLabel;
        this.text = text;
        this.question = question;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getKeyLabel() { return keyLabel; }
    public void setKeyLabel(String keyLabel) { this.keyLabel = keyLabel; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Boolean getIsCorrect() { return isCorrect; }
    public void setIsCorrect(Boolean isCorrect) { this.isCorrect = isCorrect; }

    public Integer getOrderIndex() { return orderIndex; }
    public void setOrderIndex(Integer orderIndex) { this.orderIndex = orderIndex; }

    public Question getQuestion() { return question; }
    public void setQuestion(Question question) { this.question = question; }
}
