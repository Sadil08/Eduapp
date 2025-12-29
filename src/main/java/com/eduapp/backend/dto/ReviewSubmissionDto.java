package com.eduapp.backend.dto;

import jakarta.validation.constraints.*;

public class ReviewSubmissionDto {

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;

    @NotBlank(message = "Review text is required")
    @Size(min = 10, max = 500, message = "Review text must be between 10 and 500 characters")
    private String reviewText;

    // Constructors
    public ReviewSubmissionDto() {
    }

    public ReviewSubmissionDto(Integer rating, String reviewText) {
        this.rating = rating;
        this.reviewText = reviewText;
    }

    // Getters and Setters
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
}
