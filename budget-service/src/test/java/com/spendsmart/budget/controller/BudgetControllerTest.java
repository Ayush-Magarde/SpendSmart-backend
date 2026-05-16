package com.spendsmart.budget.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.service.BudgetService;
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

@WebMvcTest(controllers = BudgetController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class BudgetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BudgetService budgetService;

    private Budget budget;
    private BudgetRequest request;
    private final Long USER_ID = 1L;
    private final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        request = new BudgetRequest();
        request.setName("Food Budget");
        request.setAmount(1000.0);
        request.setCategory("Food");
        request.setAlertThreshold(800.0);

        budget = new Budget();
        budget.setId(10L);
        budget.setUserId(USER_ID);
        budget.setName("Food Budget");
        budget.setAmount(1000.0);
        budget.setCategory("Food");
        budget.setAlertThreshold(800.0);
        budget.setSpent(0.0);
    }

    @Test
    void createBudget_Success() throws Exception {
        when(budgetService.addBudget(eq(USER_ID), eq(EMAIL), any(BudgetRequest.class))).thenReturn(budget);

        mockMvc.perform(post("/api/budgets")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(budgetService, times(1)).addBudget(eq(USER_ID), eq(EMAIL), any(BudgetRequest.class));
    }

    @Test
    void getBudgets_Success() throws Exception {
        when(budgetService.getBudgets(USER_ID)).thenReturn(List.of(budget));

        mockMvc.perform(get("/api/budgets")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(budgetService, times(1)).getBudgets(USER_ID);
    }

    @Test
    void updateBudget_Success() throws Exception {
        when(budgetService.updateBudget(eq(10L), eq(USER_ID), eq(EMAIL), any(BudgetRequest.class))).thenReturn(budget);

        mockMvc.perform(put("/api/budgets/10")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));

        verify(budgetService, times(1)).updateBudget(eq(10L), eq(USER_ID), eq(EMAIL), any(BudgetRequest.class));
    }

    @Test
    void deleteBudget_Success() throws Exception {
        doNothing().when(budgetService).deleteBudget(10L, USER_ID);

        mockMvc.perform(delete("/api/budgets/10")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID))
                .andExpect(status().isOk());

        verify(budgetService, times(1)).deleteBudget(10L, USER_ID);
    }

    @Test
    void updateSpentAmount_Success() throws Exception {
        doNothing().when(budgetService).updateSpentAmount(USER_ID, "Food", 500.0, EMAIL);

        mockMvc.perform(put("/api/budgets/update-spent/{userId}/{category}/{amount}", USER_ID, "Food", 500.0)
                .header("X-User-Email", EMAIL))
                .andExpect(status().isOk());

        verify(budgetService, times(1)).updateSpentAmount(USER_ID, "Food", 500.0, EMAIL);
    }
}
