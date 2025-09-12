package com.finance.tracker.category.repository;

import com.finance.tracker.category.domain.entities.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CategoryRepository extends JpaRepository<Category, String> {

    @Query(value = "SELECT * FROM category WHERE _id = :categoryId AND (user_id IS NULL OR user_id = :userId) LIMIT 1", nativeQuery = true)
    Category getCategoryByCategoryIdAndUserId(@Param("categoryId") String categoryId, @Param("userId") String userId);
}
