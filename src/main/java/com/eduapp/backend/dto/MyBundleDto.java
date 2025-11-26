package com.eduapp.backend.dto;

import com.eduapp.backend.model.PaperType;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for displaying purchased bundles on student dashboard
 * Contains only essential information without nested circular references
 */
public class MyBundleDto {
    private Long accessId;
    private Long bundleId;
    private String bundleName;
    private String bundleDescription;
    private BigDecimal price;
    private PaperType type;
    private String examType;
    private Boolean isPastPaper;
    private String subjectName;
    private String lessonName;
    private LocalDateTime purchasedAt;
    private int paperCount;

    public MyBundleDto() {
    }

    public MyBundleDto(Long accessId, Long bundleId, String bundleName, String bundleDescription,
            BigDecimal price, PaperType type, String examType, Boolean isPastPaper,
            String subjectName, String lessonName, LocalDateTime purchasedAt, int paperCount) {
        this.accessId = accessId;
        this.bundleId = bundleId;
        this.bundleName = bundleName;
        this.bundleDescription = bundleDescription;
        this.price = price;
        this.type = type;
        this.examType = examType;
        this.isPastPaper = isPastPaper;
        this.subjectName = subjectName;
        this.lessonName = lessonName;
        this.purchasedAt = purchasedAt;
        this.paperCount = paperCount;
    }

    // Getters and Setters
    public Long getAccessId() {
        return accessId;
    }

    public void setAccessId(Long accessId) {
        this.accessId = accessId;
    }

    public Long getBundleId() {
        return bundleId;
    }

    public void setBundleId(Long bundleId) {
        this.bundleId = bundleId;
    }

    public String getBundleName() {
        return bundleName;
    }

    public void setBundleName(String bundleName) {
        this.bundleName = bundleName;
    }

    public String getBundleDescription() {
        return bundleDescription;
    }

    public void setBundleDescription(String bundleDescription) {
        this.bundleDescription = bundleDescription;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public PaperType getType() {
        return type;
    }

    public void setType(PaperType type) {
        this.type = type;
    }

    public String getExamType() {
        return examType;
    }

    public void setExamType(String examType) {
        this.examType = examType;
    }

    public Boolean getIsPastPaper() {
        return isPastPaper;
    }

    public void setIsPastPaper(Boolean isPastPaper) {
        this.isPastPaper = isPastPaper;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public LocalDateTime getPurchasedAt() {
        return purchasedAt;
    }

    public void setPurchasedAt(LocalDateTime purchasedAt) {
        this.purchasedAt = purchasedAt;
    }

    public int getPaperCount() {
        return paperCount;
    }

    public void setPaperCount(int paperCount) {
        this.paperCount = paperCount;
    }
}
