package com.finance.tracker.category.service;

import com.finance.tracker.category.domain.CategoryCreateUpdateRequest;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.transactions.domain.TransactionType;

public interface CategoryService {

    void validateCategoryForTransaction(String userId, String categoryId, TransactionType type);

    Category createCategory(CategoryCreateUpdateRequest request);
}
