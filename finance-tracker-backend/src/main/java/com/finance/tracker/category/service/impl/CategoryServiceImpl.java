package com.finance.tracker.category.service.impl;

import com.finance.tracker.category.domain.CategoryCreateUpdateRequest;
import com.finance.tracker.category.domain.CategoryType;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.category.exceptions.CategoryNotActiveException;
import com.finance.tracker.category.exceptions.CategoryNotForExpenseException;
import com.finance.tracker.category.exceptions.CategoryNotForIncomeException;
import com.finance.tracker.category.exceptions.CategoryNotFoundException;
import com.finance.tracker.category.repository.CategoryRepository;
import com.finance.tracker.category.service.CategoryService;
import com.finance.tracker.transactions.domain.TransactionType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public void validateCategoryForTransaction(String userId, String categoryId, TransactionType type) {
        Optional<Category> optionalCategory = Optional.ofNullable(categoryRepository.getCategoryByCategoryIdAndUserId(categoryId, userId));
        Category category = optionalCategory.orElseThrow(() -> new CategoryNotFoundException("Not Found"));

        if(null != category.getDeletedAt() || false == category.getActive()){
            throw new CategoryNotActiveException("Category is not Active");
        }
        if(!type.name().equals(category.getType().name()) && type.getValue().equalsIgnoreCase("income")){
            throw new CategoryNotForIncomeException("Not for income");
        }
        if(!type.name().equals(category.getType().name()) && type.getValue().equalsIgnoreCase("expense")){
            throw new CategoryNotForExpenseException("not for expense");
        }

    }

    @Override
    public Category createCategory(CategoryCreateUpdateRequest request) {
        Category category = Category.builder()
                .label(request.label())
                .key(request.label())
                .active(true)
                .isIncome(request.categoryType().equalsIgnoreCase("income"))
                .isExpense(request.categoryType().equalsIgnoreCase("expense"))
                .createdAt(Instant.now())
                .deletedAt(null)
                .updatedAt(Instant.now())
                .userId(null)
                .parentId(null)
                .type(CategoryType.fromValueIgnoreCase(request.categoryType()))
                .build();
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }
}
