package com.spendsmart.expense.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.expense.client.CategoryServiceClient;
import com.spendsmart.expense.dto.CategoryDTO;
import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.service.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ExpenseController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class ExpenseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExpenseService expenseService;

    @MockBean
    private CategoryServiceClient categoryServiceClient;

    private Expense expense;
    private ExpenseRequest request;
    private final Long USER_ID = 1L;
    private final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        request = new ExpenseRequest();
        request.setAmount(500.0);
        request.setCategory("Food");
        request.setDescription("Groceries");

        expense = new Expense();
        expense.setId(10L);
        expense.setUserId(USER_ID);
        expense.setAmount(500.0);
        expense.setCategory("Food");
        expense.setDescription("Groceries");
    }

    @Test
    void addExpense_Success() throws Exception {
        when(expenseService.addExpense(eq(USER_ID), eq(EMAIL), any(ExpenseRequest.class))).thenReturn(expense);

        mockMvc.perform(post("/api/expenses")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(expenseService, times(1)).addExpense(eq(USER_ID), eq(EMAIL), any(ExpenseRequest.class));
    }

    @Test
    void getExpenses_Success() throws Exception {
        when(expenseService.getExpenses(USER_ID)).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/expenses")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(expenseService, times(1)).getExpenses(USER_ID);
    }

    @Test
    void deleteExpense_Success() throws Exception {
        doNothing().when(expenseService).deleteExpense(10L, USER_ID, EMAIL);

        mockMvc.perform(delete("/api/expenses/10")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID))
                .andExpect(status().isOk());

        verify(expenseService, times(1)).deleteExpense(10L, USER_ID, EMAIL);
    }

    @Test
    void getAllExpenses_Admin_Success() throws Exception {
        when(expenseService.getAllExpenses()).thenReturn(List.of(expense));

        mockMvc.perform(get("/api/expenses/admin/all")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getAllExpenses_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/expenses/admin/all")
                .header("X-User-Role", "USER"))
                .andExpect(status().isUnauthorized()); // Or 500 if no GlobalExceptionHandler. Assuming one exists or maps to 401.
    }

    @Test
    void getExpenseCount_Admin_Success() throws Exception {
        when(expenseService.getExpenseCount()).thenReturn(5L);

        mockMvc.perform(get("/api/expenses/admin/count")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void getExpenseCount_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/expenses/admin/count")
                .header("X-User-Role", "USER"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getExpenseCategories_Success() throws Exception {
        CategoryDTO cat = new CategoryDTO();
        cat.setName("Food");
        when(categoryServiceClient.getCategoriesByType(USER_ID, "EXPENSE")).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/expenses/categories")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}
