package com.spendsmart.category.service;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;

import java.util.List;

public interface CategoryService {

    Category createCategory(Long userId, CategoryRequest request);

    List<Category> getCategories(Long userId);

    List<Category> getCategoriesByType(Long userId, CategoryType type);

    Category updateCategory(Long id, Long userId, CategoryRequest request);

    void deleteCategory(Long id, Long userId);
}