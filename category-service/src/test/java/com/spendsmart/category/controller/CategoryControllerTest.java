package com.spendsmart.category.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.category.dto.CategoryRequest;
import com.spendsmart.category.entity.Category;
import com.spendsmart.category.entity.CategoryType;
import com.spendsmart.category.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CategoryController.class)
@Import(com.spendsmart.category.config.SecurityConfig.class)
class CategoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CategoryService categoryService;

    @Autowired
    private ObjectMapper objectMapper;

    private Category sampleCategory;
    private CategoryRequest request;

    @BeforeEach
    void setUp() {
        sampleCategory = new Category(1L, 42L, "Food", "EXPENSE", 500.0, false);

        request = new CategoryRequest();
        request.setName("Food");
        request.setType("EXPENSE");
        request.setBudgetLimit(500.0);
    }

    // ── POST /api/categories ─────────────────────────────────────────────────

    @Test
    void createCategory_returns200WithCategory() throws Exception {
        when(categoryService.createCategory(eq(42L), any(CategoryRequest.class)))
                .thenReturn(sampleCategory);

        mockMvc.perform(post("/api/categories")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Food"));

        verify(categoryService).createCategory(eq(42L), any(CategoryRequest.class));
    }

    // ── GET /api/categories ──────────────────────────────────────────────────

    @Test
    void getCategories_returns200WithList() throws Exception {
        when(categoryService.getCategories(42L)).thenReturn(List.of(sampleCategory));

        mockMvc.perform(get("/api/categories")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Food"));

        verify(categoryService).getCategories(42L);
    }

    // ── GET /api/categories/type/{type} ──────────────────────────────────────

    @Test
    void getCategoriesByType_returnsFilteredList() throws Exception {
        when(categoryService.getCategoriesByType(42L, CategoryType.EXPENSE))
                .thenReturn(List.of(sampleCategory));

        mockMvc.perform(get("/api/categories/type/EXPENSE")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Food"));

        verify(categoryService).getCategoriesByType(42L, CategoryType.EXPENSE);
    }

    @Test
    void getCategoriesByType_worksWithoutOptionalEmailHeader() throws Exception {
        when(categoryService.getCategoriesByType(42L, CategoryType.INCOME))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/categories/type/INCOME")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // ── PUT /api/categories/{id} ─────────────────────────────────────────────

    @Test
    void updateCategory_returns200WithUpdated() throws Exception {
        Category updated = new Category(1L, 42L, "Transport", "EXPENSE", 200.0, false);
        when(categoryService.updateCategory(eq(1L), eq(42L), any(CategoryRequest.class)))
                .thenReturn(updated);

        CategoryRequest updateReq = new CategoryRequest();
        updateReq.setName("Transport");
        updateReq.setType("EXPENSE");
        updateReq.setBudgetLimit(200.0);

        mockMvc.perform(put("/api/categories/1")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Transport"));

        verify(categoryService).updateCategory(eq(1L), eq(42L), any(CategoryRequest.class));
    }

    @Test
    void updateCategory_returns400WhenNotFound() throws Exception {
        when(categoryService.updateCategory(eq(99L), eq(42L), any(CategoryRequest.class)))
                .thenThrow(new RuntimeException("Category not found"));

        mockMvc.perform(put("/api/categories/99")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Id", "42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ── DELETE /api/categories/{id} ──────────────────────────────────────────

    @Test
    void deleteCategory_returns200OnSuccess() throws Exception {
        doNothing().when(categoryService).deleteCategory(1L, 42L);

        mockMvc.perform(delete("/api/categories/1")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Id", "42"))
                .andExpect(status().isOk());

        verify(categoryService).deleteCategory(1L, 42L);
    }

    @Test
    void deleteCategory_returns400WhenNotFound() throws Exception {
        doThrow(new RuntimeException("Category not found"))
                .when(categoryService).deleteCategory(99L, 42L);

        mockMvc.perform(delete("/api/categories/99")
                        .header("X-User-Email", "user@test.com")
                        .header("X-User-Id", "42"))
                .andExpect(status().isBadRequest());
    }
}
