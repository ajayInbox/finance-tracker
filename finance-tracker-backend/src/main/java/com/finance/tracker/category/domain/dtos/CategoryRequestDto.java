package com.finance.tracker.category.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotBlank(message = "Type (INCOME/EXPENSE) is required")
    private String type;

    // Only the ID of the parent is needed
    private UUID parentId;
    private String iconKey = "default-folder";
    private String colorCode = "#808080";
}