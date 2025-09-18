package com.finance.tracker.category.controller;

import com.finance.tracker.category.domain.CategoryCreateUpdateRequest;
import com.finance.tracker.category.domain.dtos.CategoryDto;
import com.finance.tracker.category.domain.entities.Category;
import com.finance.tracker.category.mapper.CategoryMapper;
import com.finance.tracker.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    @PostMapping("/category")
    public ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryCreateUpdateRequest request){
        Category category = categoryService.createCategory(request);
        return new ResponseEntity<>(categoryMapper.toDto(category), HttpStatus.CREATED);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<CategoryDto>> getAllCategories(){
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDto> res = categories.stream().map(categoryMapper::toDto).toList();
        return ResponseEntity.ok(res);
    }
}
