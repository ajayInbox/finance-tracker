package com.finance.tracker.category.domain.dtos;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CategoryDto {

    private String id;
    private String key;
    private String label;

    private String parentId;

    private Boolean isExpense;
    private Boolean isIncome;
    private Boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String metadata;
}
