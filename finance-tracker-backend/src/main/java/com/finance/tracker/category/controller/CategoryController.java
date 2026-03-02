package com.finance.tracker.category.controller;

import com.finance.tracker.category.domain.dtos.CategoryRequestDto;
import com.finance.tracker.category.domain.dtos.CategoryResponseDto;
import com.finance.tracker.category.domain.dtos.CategoryUpdateDto;
import com.finance.tracker.category.mapper.CategoryMapper;
import com.finance.tracker.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;
    private final CategoryMapper categoryMapper;

    // CREATE (Works for both Groups and Sub-categories)
    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@RequestBody CategoryRequestDto categoryRequestDTO) {
        return ResponseEntity.ok(categoryService.save(categoryRequestDTO, UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4")));
    }

    // GET ALL (Returns Groups with their children nested)
    @GetMapping()
    public ResponseEntity<List<CategoryResponseDto>> getAll() {
        return ResponseEntity.ok(categoryService.getAllTree(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4")));
    }

    // GET ALL CHILDREN ONLY
    @GetMapping("/subcategories")
    public ResponseEntity<List<CategoryResponseDto>> getAllChildren() {
        List<CategoryResponseDto> subCategories = categoryService.getAllSubCategories(UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4"));
        return ResponseEntity.ok(subCategories);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> update(@PathVariable("id") UUID id, @RequestBody CategoryUpdateDto updateDto) {
        return ResponseEntity.ok(categoryService.update(id, updateDto, UUID.fromString("960bbe86-b62c-4171-a8e5-94c4bfd3bdb4")));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        categoryService.deleteRecursive(id);
        return ResponseEntity.noContent().build();
    }
}
