package com.eduapp.backend.dto;

import java.time.LocalDateTime;

public class PublicReviewDto {
    private String userName;
    private Integer rating;
    private String reviewText;
    private LocalDateTime createdAt;

    // Constructors
    public PublicReviewDto() {
    }

    public PublicReviewDto(String userName, Integer rating, String reviewText, LocalDateTime createdAt) {
        this.userName = userName;
        this.rating = rating;
        this.reviewText = reviewText;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getReviewText() {
        return reviewText;
    }

    public void setReviewText(String reviewText) {
        this.reviewText = reviewText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
