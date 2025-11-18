package com.eduapp.backend.model;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paper_id", nullable = false)
    @JsonIgnore
    private Paper paper;

    @Column(length = 4000, nullable = false)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private QuestionType type;

    @Column(length = 4000)
    private String correctAnswerText;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<QuestionOption> options = new ArrayList<>();

    public Question() {}

    public Question(Paper paper, String text, QuestionType type, String correctAnswerText) {
        this.paper = paper;
        this.text = text;
        this.type = type;
        this.correctAnswerText = correctAnswerText;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Paper getPaper() { return paper; }
    public void setPaper(Paper paper) { this.paper = paper; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public QuestionType getType() { return type; }
    public void setType(QuestionType type) { this.type = type; }

    public String getCorrectAnswerText() { return correctAnswerText; }
    public void setCorrectAnswerText(String correctAnswerText) { this.correctAnswerText = correctAnswerText; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<QuestionOption> getOptions() { return options; }
    public void setOptions(List<QuestionOption> options) { this.options = options; }
}
