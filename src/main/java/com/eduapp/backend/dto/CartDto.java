package com.eduapp.backend.dto;

import java.util.List;

/**
 * Data Transfer Object for Cart.
 * Avoids Jackson serialization issues with Hibernate proxies.
 */
public class CartDto {

    private Long id;
    private Long userId;
    private List<CartBundleDto> bundles;

    public CartDto() {
    }

    public CartDto(Long id, Long userId, List<CartBundleDto> bundles) {
        this.id = id;
        this.userId = userId;
        this.bundles = bundles;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public List<CartBundleDto> getBundles() {
        return bundles;
    }

    public void setBundles(List<CartBundleDto> bundles) {
        this.bundles = bundles;
    }

    /**
     * Nested DTO for bundle items in the cart.
     */
    public static class CartBundleDto {
        private Long id;
        private String name;
        private String description;
        private java.math.BigDecimal price;
        private String type;
        private String examTypeName;
        private Boolean isPastPaper;

        public CartBundleDto() {
        }

        public CartBundleDto(Long id, String name, String description, java.math.BigDecimal price,
                String type, String examTypeName, Boolean isPastPaper) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.price = price;
            this.type = type;
            this.examTypeName = examTypeName;
            this.isPastPaper = isPastPaper;
        }

        // Getters and Setters
        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public java.math.BigDecimal getPrice() {
            return price;
        }

        public void setPrice(java.math.BigDecimal price) {
            this.price = price;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getExamTypeName() {
            return examTypeName;
        }

        public void setExamTypeName(String examTypeName) {
            this.examTypeName = examTypeName;
        }

        public Boolean getIsPastPaper() {
            return isPastPaper;
        }

        public void setIsPastPaper(Boolean isPastPaper) {
            this.isPastPaper = isPastPaper;
        }
    }
}
