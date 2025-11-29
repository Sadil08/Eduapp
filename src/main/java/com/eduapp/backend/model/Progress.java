package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progresses")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "bundle_id", nullable = false)
    private PaperBundle bundle;

    @ManyToOne
    @JoinColumn(name = "paper_id", nullable = false)
    private Paper paper;

    @Enumerated(EnumType.STRING)
    private ProgressStatus status = ProgressStatus.NOT_ATTEMPTED;

    @Column
    private Float completionPercentage = 0.0f;

    @Column
    private Integer timeSpentMinutes = 0;

    private LocalDateTime updatedAt = LocalDateTime.now();

    public Progress() {}

    public Progress(User user, PaperBundle bundle, Paper paper) {
        this.user = user;
        this.bundle = bundle;
        this.paper = paper;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public PaperBundle getBundle() { return bundle; }
    public void setBundle(PaperBundle bundle) { this.bundle = bundle; }

    public Paper getPaper() { return paper; }
    public void setPaper(Paper paper) { this.paper = paper; }

    public ProgressStatus getStatus() { return status; }
    public void setStatus(ProgressStatus status) { this.status = status; }

    public Float getCompletionPercentage() { return completionPercentage; }
    public void setCompletionPercentage(Float completionPercentage) { this.completionPercentage = completionPercentage; }

    public Integer getTimeSpentMinutes() { return timeSpentMinutes; }
    public void setTimeSpentMinutes(Integer timeSpentMinutes) { this.timeSpentMinutes = timeSpentMinutes; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}