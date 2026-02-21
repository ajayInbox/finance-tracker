package com.finance.tracker.category.domain.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDto {
    private UUID id;
    private String name;
    private String description;
    private String type;
    private boolean isActive;
    private UUID parentId; // Just the ID, not the whole object
    private List<CategoryResponseDto> children;
    private String iconKey;
    private String colorCode;
}