package com.finance.tracker.category.repository;

import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.domain.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    // Finds all categories that belong to a parent (sub-categories)
    List<Category> findByUserIdAndParentIsNotNullAndDeletedAtIsNullAndIsActiveTrue(UUID userId);

    // Find all children of a parent (used for manual cascade)
    List<Category> findByParentId(UUID parentId);

    // Finds a category only if it matches ID, User, AND Type
    Optional<Category> findByIdAndUserIdAndType(UUID id, UUID userId, CategoryType type);

    // Finds a category only if it matches ID, User
    Optional<Category> findByIdAndUserId(UUID id, UUID userId);

    List<Category> findAllByUserIdAndDeletedAtIsNullAndIsActiveTrue(UUID userId);
}
