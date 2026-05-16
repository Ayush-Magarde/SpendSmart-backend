package com.spendsmart.category.service;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Category createCategory(Long userId, CategoryRequest request) {
        log.info("Creating category for user: {}", userId);

        Category category = new Category();
        category.setUserId(userId);
        category.setName(request.getName());
        category.setType(request.getType());
        category.setBudgetLimit(request.getBudgetLimit());
        category.setIsDefault(false);

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getId());

        return savedCategory;
    }

    @Override
    @org.springframework.transaction.annotation.Transactional
    public List<Category> getCategories(Long userId) {
        log.info("Retrieving categories for user: {}", userId);
        List<Category> categories = categoryRepository.findByUserId(userId);
        
        if (categories.isEmpty()) {
            log.info("No categories found for user {}, seeding defaults...", userId);
            seedDefaultCategories(userId);
            return categoryRepository.findByUserId(userId);
        }
        
        return categories;
    }

    private void seedDefaultCategories(Long userId) {
        String[] expenseDefaults = {"Food", "Transportation", "Rent", "Travel", "Other"};
        String[] incomeDefaults = {"Salary", "Freelance", "Gifts", "Other"};

        for (String name : expenseDefaults) {
            categoryRepository.save(new Category(null, userId, name, "EXPENSE", 0.0, true));
        }
        for (String name : incomeDefaults) {
            categoryRepository.save(new Category(null, userId, name, "INCOME", 0.0, true));
        }
    }

    @Override
    public List<Category> getCategoriesByType(Long userId, CategoryType type) {
        return categoryRepository.findByUserIdAndType(userId, type.name());
    }

    @Override
    public Category updateCategory(Long id, Long userId, CategoryRequest request) {
        log.info("Updating category {} for user: {}", id, userId);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // For simplicity, we'll skip authorization check for now
        // In production, you'd verify the category belongs to the user

        category.setName(request.getName());
        category.setType(request.getType());
        category.setBudgetLimit(request.getBudgetLimit());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getId());

        return updatedCategory;
    }

    @Override
    public void deleteCategory(Long id, Long userId) {
        log.info("Deleting category {} for user: {}", id, userId);
        
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // For simplicity, we'll skip authorization check for now
        // In production, you'd verify the category belongs to the user

        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }
}
