package com.spendsmart.income.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spendsmart.income.client.CategoryServiceClient;
import com.spendsmart.income.dto.CategoryDTO;
import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.service.IncomeService;
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

@WebMvcTest(controllers = IncomeController.class, excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class IncomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private IncomeService incomeService;

    @MockBean
    private CategoryServiceClient categoryServiceClient;

    private Income income;
    private IncomeRequest request;
    private final Long USER_ID = 1L;
    private final String EMAIL = "test@example.com";

    @BeforeEach
    void setUp() {
        request = new IncomeRequest();
        request.setAmount(1000.0);
        request.setCategory("Salary");
        request.setDescription("Test Income");

        income = new Income();
        income.setId(10L);
        income.setUserId(USER_ID);
        income.setAmount(1000.0);
        income.setCategory("Salary");
        income.setDescription("Test Income");
    }

    @Test
    void addIncome_Success() throws Exception {
        when(incomeService.addIncome(eq(USER_ID), any(IncomeRequest.class))).thenReturn(income);

        mockMvc.perform(post("/api/incomes")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L))
                .andExpect(jsonPath("$.amount").value(1000.0))
                .andExpect(jsonPath("$.category").value("Salary"));

        verify(incomeService, times(1)).addIncome(eq(USER_ID), any(IncomeRequest.class));
    }

    @Test
    void getIncomes_Success() throws Exception {
        when(incomeService.getIncomes(USER_ID)).thenReturn(List.of(income));

        mockMvc.perform(get("/api/incomes")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(10L));

        verify(incomeService, times(1)).getIncomes(USER_ID);
    }

    @Test
    void deleteIncome_Success() throws Exception {
        doNothing().when(incomeService).deleteIncome(10L, USER_ID);

        mockMvc.perform(delete("/api/incomes/10")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID))
                .andExpect(status().isOk());

        verify(incomeService, times(1)).deleteIncome(10L, USER_ID);
    }

    @Test
    void getAllIncomes_Admin_Success() throws Exception {
        when(incomeService.getAllIncomes()).thenReturn(List.of(income));

        mockMvc.perform(get("/api/incomes/admin/all")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));

        verify(incomeService, times(1)).getAllIncomes();
    }

    @Test
    void getAllIncomes_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/incomes/admin/all")
                .header("X-User-Role", "USER"))
                .andExpect(status().isUnauthorized());

        verify(incomeService, never()).getAllIncomes();
    }

    @Test
    void getIncomeCount_Admin_Success() throws Exception {
        when(incomeService.getIncomeCount()).thenReturn(5L);

        mockMvc.perform(get("/api/incomes/admin/count")
                .header("X-User-Role", "ADMIN"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));

        verify(incomeService, times(1)).getIncomeCount();
    }

    @Test
    void getIncomeCount_User_Forbidden() throws Exception {
        mockMvc.perform(get("/api/incomes/admin/count")
                .header("X-User-Role", "USER"))
                .andExpect(status().isUnauthorized());

        verify(incomeService, never()).getIncomeCount();
    }

    @Test
    void getIncomeCategories_Success() throws Exception {
        CategoryDTO cat = new CategoryDTO();
        cat.setName("Salary");
        when(categoryServiceClient.getCategoriesByType(USER_ID, "INCOME")).thenReturn(List.of(cat));

        mockMvc.perform(get("/api/incomes/categories")
                .header("X-User-Email", EMAIL)
                .header("X-User-Id", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Salary"));

        verify(categoryServiceClient, times(1)).getCategoriesByType(USER_ID, "INCOME");
    }
}
