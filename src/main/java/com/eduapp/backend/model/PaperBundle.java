package com.eduapp.backend.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "paper_bundles")
public class PaperBundle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    private PaperType type;

    @Column
    private String examType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @Column
    private Boolean isPastPaper = false;

    @ManyToOne
    @JoinColumn(name = "created_by")
    private User createdBy;

    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "bundle", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Paper> papers = new ArrayList<>();

    public PaperBundle() {}

    public PaperBundle(String name, String description, BigDecimal price, PaperType type, String examType, Subject subject, Lesson lesson, Boolean isPastPaper) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
        this.examType = examType;
        this.subject = subject;
        this.lesson = lesson;
        this.isPastPaper = isPastPaper != null ? isPastPaper : false;
    }

    // getters/setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public PaperType getType() { return type; }
    public void setType(PaperType type) { this.type = type; }

    public String getExamType() { return examType; }
    public void setExamType(String examType) { this.examType = examType; }

    public Subject getSubject() { return subject; }
    public void setSubject(Subject subject) { this.subject = subject; }

    public Lesson getLesson() { return lesson; }
    public void setLesson(Lesson lesson) { this.lesson = lesson; }

    public Boolean getIsPastPaper() { return isPastPaper; }
    public void setIsPastPaper(Boolean isPastPaper) { this.isPastPaper = isPastPaper; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public List<Paper> getPapers() { return papers; }
    public void setPapers(List<Paper> papers) { this.papers = papers; }
}
