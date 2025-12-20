package com.finance.tracker.category.domain.dtos;

import lombok.Data;

import java.time.LocalDateTime;

import com.finance.tracker.category.domain.CategoryType;

@Data
public class CategoryDto {

    private String id;
    private String key;
    private String label;

    private String parentId;
    private CategoryType categoryType;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String metadata;
}
