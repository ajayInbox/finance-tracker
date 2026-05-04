package com.finance.tracker.category.controller;

import com.finance.tracker.category.domain.dtos.CategoryRequestDto;
import com.finance.tracker.category.domain.dtos.CategoryResponseDto;
import com.finance.tracker.category.domain.dtos.CategoryUpdateDto;
import com.finance.tracker.category.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/categories")
@CrossOrigin
public class CategoryController {

    private final CategoryService categoryService;

    // CREATE (Works for both Groups and Sub-categories)
    @PostMapping
    public ResponseEntity<CategoryResponseDto> create(@RequestBody CategoryRequestDto categoryRequestDTO, Authentication auth) {
        return ResponseEntity.ok(categoryService.save(categoryRequestDTO, UUID.fromString((String) auth.getPrincipal())));
    }

    // GET ALL (Returns Groups with their children nested)
    @GetMapping()
    public ResponseEntity<List<CategoryResponseDto>> getAll(Authentication auth) {
        return ResponseEntity.ok(categoryService.getAllTree(UUID.fromString((String) auth.getPrincipal())));
    }

    // GET ALL CHILDREN ONLY
    @GetMapping("/subcategories")
    public ResponseEntity<List<CategoryResponseDto>> getAllChildren(Authentication auth) {
        List<CategoryResponseDto> subCategories = categoryService.getAllSubCategories(UUID.fromString((String) auth.getPrincipal()));
        return ResponseEntity.ok(subCategories);
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDto> update(@PathVariable("id") UUID id, @RequestBody CategoryUpdateDto updateDto,  Authentication auth) {
        return ResponseEntity.ok(categoryService.update(id, updateDto, UUID.fromString((String) auth.getPrincipal())));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") UUID id) {
        categoryService.deleteRecursive(id);
        return ResponseEntity.noContent().build();
    }
}
