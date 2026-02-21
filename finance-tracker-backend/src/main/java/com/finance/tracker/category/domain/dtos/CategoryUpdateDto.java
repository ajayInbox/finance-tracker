package com.finance.tracker.category.domain.dtos;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryUpdateDto {
    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    private Boolean isActive; // Allow toggling without deleting

    private UUID parentId; // Allow moving to a different group (optional)

    private String iconKey = "default-folder";
    private String colorCode = "#808080";
}