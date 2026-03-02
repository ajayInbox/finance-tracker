package com.finance.tracker.category.service;

import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.domain.dtos.CategoryRequestDto;
import com.finance.tracker.category.domain.dtos.CategoryResponseDto;
import com.finance.tracker.category.domain.dtos.CategoryUpdateDto;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.transactions.domain.TransactionType;

import java.util.List;
import java.util.UUID;

public interface CategoryService {

    Category validateAndGet(UUID userId, UUID categoryId, CategoryType type);

    CategoryResponseDto save(CategoryRequestDto dto, UUID userId);

    List<CategoryResponseDto> getAllTree(UUID userId);

    CategoryResponseDto update(UUID id, CategoryUpdateDto updateDto, UUID userId);

    void deleteRecursive(UUID id);

    List<CategoryResponseDto> getAllSubCategories(UUID userId);
}
