package com.spendsmart.category.controller;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management", description = "APIs for managing categories")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;

    @Operation(summary = "Create a new category")
    @PostMapping
    public Category createCategory(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody CategoryRequest request) {

        log.info("Creating category for user: {}", email);
        return categoryService.createCategory(userId, request);
    }

    @GetMapping
    @Operation(summary = "Get all categories for user")
    public List<Category> getCategories(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Retrieving categories for user: {}", email);
        return categoryService.getCategories(userId);
    }

    @GetMapping("/type/{type}")
    @Operation(summary = "Get categories by type for user")
    public List<Category> getCategoriesByType(
            @RequestHeader(value = "X-User-Email", required = false) String email,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable CategoryType type) {

        return categoryService.getCategoriesByType(userId, type);
    }

    @PutMapping("/{id}")
    public Category updateCategory(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @RequestBody CategoryRequest request) {

        return categoryService.updateCategory(id, userId, request);
    }

    @DeleteMapping("/{id}")
    public void deleteCategory(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {

        categoryService.deleteCategory(id, userId);
    }
}