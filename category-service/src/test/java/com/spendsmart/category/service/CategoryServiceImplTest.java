package com.spendsmart.category.service;

import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryServiceImpl categoryService;

    private CategoryRequest request;
    private Category existingCategory;

    @BeforeEach
    void setUp() {
        request = new CategoryRequest();
        request.setName("Food");
        request.setType("EXPENSE");
        request.setBudgetLimit(500.0);

        existingCategory = new Category(1L, 42L, "Food", "EXPENSE", 500.0, false);
    }

    // ── createCategory ──────────────────────────────────────────────────────

    @Test
    void createCategory_savesAndReturnsCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        Category result = categoryService.createCategory(42L, request);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Food");
        assertThat(result.getId()).isEqualTo(1L);
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void createCategory_setsIsDefaultToFalse() {
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        Category result = categoryService.createCategory(42L, request);

        assertThat(result.getIsDefault()).isFalse();
    }

    // ── getCategories ────────────────────────────────────────────────────────

    @Test
    void getCategories_returnsExistingCategories() {
        when(categoryRepository.findByUserId(42L)).thenReturn(List.of(existingCategory));

        List<Category> result = categoryService.getCategories(42L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Food");
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void getCategories_seedsDefaultsWhenNoneExist() {
        when(categoryRepository.findByUserId(42L))
                .thenReturn(Collections.emptyList())
                .thenReturn(List.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenAnswer(inv -> inv.getArgument(0));

        List<Category> result = categoryService.getCategories(42L);

        // 5 expense + 4 income defaults saved
        verify(categoryRepository, times(9)).save(any(Category.class));
        assertThat(result).hasSize(1);
    }

    // ── getCategoriesByType ──────────────────────────────────────────────────

    @Test
    void getCategoriesByType_returnsFilteredList() {
        when(categoryRepository.findByUserIdAndType(42L, "EXPENSE"))
                .thenReturn(List.of(existingCategory));

        List<Category> result = categoryService.getCategoriesByType(42L, CategoryType.EXPENSE);

        assertThat(result).hasSize(1);
        verify(categoryRepository).findByUserIdAndType(42L, "EXPENSE");
    }

    @Test
    void getCategoriesByType_returnsEmptyForIncomeWhenNoneExist() {
        when(categoryRepository.findByUserIdAndType(42L, "INCOME"))
                .thenReturn(Collections.emptyList());

        List<Category> result = categoryService.getCategoriesByType(42L, CategoryType.INCOME);

        assertThat(result).isEmpty();
    }

    // ── updateCategory ───────────────────────────────────────────────────────

    @Test
    void updateCategory_updatesAndSavesSuccessfully() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(categoryRepository.save(any(Category.class))).thenReturn(existingCategory);

        CategoryRequest updateRequest = new CategoryRequest();
        updateRequest.setName("Transport");
        updateRequest.setType("EXPENSE");
        updateRequest.setBudgetLimit(200.0);

        Category result = categoryService.updateCategory(1L, 42L, updateRequest);

        assertThat(result).isNotNull();
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void updateCategory_throwsWhenCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.updateCategory(99L, 42L, request))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }

    // ── deleteCategory ───────────────────────────────────────────────────────

    @Test
    void deleteCategory_deletesSuccessfully() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));

        categoryService.deleteCategory(1L, 42L);

        verify(categoryRepository).delete(existingCategory);
    }

    @Test
    void deleteCategory_throwsWhenCategoryNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.deleteCategory(99L, 42L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Category not found");
    }
}
