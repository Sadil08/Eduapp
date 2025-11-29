package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "leaderboard_entries")
public class LeaderboardEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @Column
    private Integer score;

    @ManyToOne
    @JoinColumn(name = "subject_id", nullable = false)
    private Subject subject;

    @Column
    private Boolean isAnonymous = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    public LeaderboardEntry() {}

    public LeaderboardEntry(User user, Paper paper, Integer score, Subject subject, Boolean isAnonymous) {
        this.user = user;
        this.paper = paper;
        this.score = score;
        this.subject = subject;
        this.isAnonymous = isAnonymous != null ? isAnonymous : false;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Paper getPaper() { return paper; }
    public void setPaper(Paper paper) { this.paper = paper; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Boolean getIsAnonymous() { return isAnonymous; }
    public void setIsAnonymous(Boolean isAnonymous) { this.isAnonymous = isAnonymous; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}