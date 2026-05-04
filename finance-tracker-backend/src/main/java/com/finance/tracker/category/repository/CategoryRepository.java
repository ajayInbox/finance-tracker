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
    @Query(value = "SELECT * FROM categories WHERE (user_id = :userId OR user_id IS NULL) AND parent_id IS NOT NULL AND deleted_at IS NULL AND is_active = true", nativeQuery = true)
    List<Category> findByUserIdAndParentIsNotNullAndDeletedAtIsNullAndIsActiveTrue(@Param("userId") UUID userId);

    // Find all children of a parent (used for manual cascade)
    List<Category> findByParentId(UUID parentId);

    // Finds a category only if it matches ID, User, AND Type
    @Query(value = "SELECT * FROM categories WHERE id = :id AND type = :type AND (user_id = :userId OR user_id IS NULL) AND deleted_at IS NULL AND is_active = true", nativeQuery = true)
    Optional<Category> findByIdAndUserIdOrUserIdIsNullAndType(@Param("id") UUID id, @Param("userId") UUID userId, @Param("type") String type);

    // Finds a category only if it matches ID, User
    @Query(value = "SELECT * FROM categories WHERE id = :id AND user_id = :userId AND deleted_at IS NULL AND is_active = true", nativeQuery = true)
    Optional<Category> findByIdAndUserId(@Param("id") UUID id, @Param("userId") UUID userId);

    @Query(value = "SELECT * FROM categories WHERE (user_id = :userId OR user_id IS NULL) AND deleted_at IS NULL AND is_active = true", nativeQuery = true)
    List<Category> findAllByUserIdAndDeletedAtIsNullAndIsActiveTrue(@Param("userId") UUID userId);
}
