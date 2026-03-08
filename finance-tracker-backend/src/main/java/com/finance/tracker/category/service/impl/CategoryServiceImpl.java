package com.finance.tracker.category.service.impl;

import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.domain.dtos.CategoryRequestDto;
import com.finance.tracker.category.domain.dtos.CategoryResponseDto;
import com.finance.tracker.category.domain.dtos.CategoryUpdateDto;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.category.exceptions.CategoryNotFoundException;
import com.finance.tracker.category.repository.CategoryRepository;
import com.finance.tracker.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository repository;

    // 1. CREATE
    @Override
    public CategoryResponseDto save(CategoryRequestDto dto, UUID userId) {
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        category.setType(CategoryType.fromValueIgnoreCase(dto.getType()));
        category.setUserId(userId); // Set from the secure source, not the body
        category.setIconKey(dto.getIconKey());
        category.setColorCode(dto.getColorCode());

        if (dto.getParentId() != null) {
            // Validation ensures the parent belongs to the same user AND has the same type
            Category parent = validateAndGet(userId, dto.getParentId(), CategoryType.fromValueIgnoreCase(dto.getType()));
            category.setParent(parent);
        }

        Category savedCategory = repository.save(category);
        return mapToResponseDto(savedCategory);
    }

    // 2. GET ALL (Nested Tree)
    @Override
    public List<CategoryResponseDto> getAllTree(UUID userId) {
        // 1. Fetch flat list
        List<Category> allCategories = repository.findAllByUserIdAndDeletedAtIsNull(userId);

        // 2. Map to DTOs using our unified method
        Map<UUID, CategoryResponseDto> dtoMap = allCategories.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toMap(CategoryResponseDto::getId, dto -> dto));

        List<CategoryResponseDto> roots = new ArrayList<>();

        // 3. One-pass assembly
        dtoMap.values().forEach(dto -> {
            if (dto.getParentId() == null) {
                roots.add(dto);
            } else {
                CategoryResponseDto parent = dtoMap.get(dto.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        });

        return roots;
    }

    // 3. UPDATE
    @Override
    public CategoryResponseDto update(UUID id, CategoryUpdateDto updateDto, UUID userId) {
        // 1. Validate the category exists and belongs to the user
        Category category = repository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found or access denied"));

        // 2. Update basic fields
        category.setName(updateDto.getName());
        category.setDescription(updateDto.getDescription());
        category.setIconKey(updateDto.getIconKey());
        category.setColorCode(updateDto.getColorCode());

        if (updateDto.getIsActive() != null) {
            category.setActive(updateDto.getIsActive());
        }

        // 3. Handle moving to a different parent (if allowed)
        if (updateDto.getParentId() != null && !updateDto.getParentId().equals(category.getParent().getId())) {
            // Ensure the NEW parent also belongs to the user and matches the category's type
            Category newParent = validateAndGet(userId, updateDto.getParentId(), category.getType());
            category.setParent(newParent);
        }

        Category updatedCategory = repository.save(category);
        return mapToResponseDto(updatedCategory);
    }

    // 4. DELETE (Manual Cascade)
    @Override
    public void deleteRecursive(UUID id) {
        Category category = repository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found or access denied"));

        OffsetDateTime now = OffsetDateTime.now();

        // Soft delete the target
        category.setDeletedAt(now);
        category.setActive(false);

        // Cascade to children
        if(category.getParent() == null) {
            List<Category> children = repository.findByParentId(id);
            for (Category child : children) {
                child.setDeletedAt(now);
                child.setActive(false);
                // If you had more than 2 levels, you'd call this method recursively here
            }
            repository.save(category);
            repository.saveAll(children);
        }
        else {
            repository.save(category);
        }
    }

    @Override
    public List<CategoryResponseDto> getAllSubCategories(UUID userId) {
        List<Category> children = repository.findByUserIdAndParentIsNotNullAndDeletedAtIsNull(userId);

        return children.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Validates existence and ownership.
     * Returns the category if valid, otherwise throws an exception.
     */
    @Override
    public Category validateAndGet(UUID userId, UUID id, CategoryType categoryType) {
        return repository.findByIdAndUserIdAndType(id, userId, categoryType)
                .orElseThrow(() -> new CategoryNotFoundException("Category not found or access denied"));
    }

    private CategoryResponseDto mapToResponseDto(Category entity) {
        return CategoryResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .type(entity.getType().name())
                .iconKey(entity.getIconKey())
                .colorCode(entity.getColorCode())
                .isActive(entity.isActive())
                .parentId(entity.getParent() != null ? entity.getParent().getId() : null)
                .children(new ArrayList<>()) // Always start fresh to avoid recursion loops
                .build();
    }
}
