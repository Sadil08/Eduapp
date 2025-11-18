package com.eduapp.backend.dto;

import com.eduapp.backend.model.PaperType;
import java.math.BigDecimal;
import java.util.List;

public class PaperBundleDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private PaperType type;
    private String examType;
    private Long subjectId;
    private Long lessonId;
    private Boolean isPastPaper;
    private List<PaperDto> papers;

    public PaperBundleDto() {}

    public PaperBundleDto(Long id, String name, String description, BigDecimal price, PaperType type, String examType,
                          Long subjectId, Long lessonId, Boolean isPastPaper, List<PaperDto> papers) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.type = type;
        this.examType = examType;
        this.subjectId = subjectId;
        this.lessonId = lessonId;
        this.isPastPaper = isPastPaper;
        this.papers = papers;
    }

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

    public Long getSubjectId() { return subjectId; }
    public void setSubjectId(Long subjectId) { this.subjectId = subjectId; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public Boolean getIsPastPaper() { return isPastPaper; }
    public void setIsPastPaper(Boolean isPastPaper) { this.isPastPaper = isPastPaper; }

    public List<PaperDto> getPapers() { return papers; }
    public void setPapers(List<PaperDto> papers) { this.papers = papers; }
}
