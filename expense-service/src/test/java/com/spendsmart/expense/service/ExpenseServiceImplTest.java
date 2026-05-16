package com.spendsmart.expense.service;

import com.spendsmart.expense.client.BudgetServiceClient;
import com.spendsmart.expense.client.CategoryServiceClient;
import com.spendsmart.expense.dto.CategoryDTO;
import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.repository.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private BudgetServiceClient budgetServiceClient;

    @Mock
    private CategoryServiceClient categoryServiceClient;

    @InjectMocks
    private ExpenseServiceImpl expenseService;

    private ExpenseRequest request;
    private Expense expense;
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
    void addExpense_Success() {
        CategoryDTO cat = new CategoryDTO();
        cat.setName("Food");
        when(categoryServiceClient.getCategoriesByType(USER_ID, "EXPENSE")).thenReturn(List.of(cat));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        doNothing().when(budgetServiceClient).updateSpentAmount(USER_ID, "Food", 500.0, EMAIL);

        Expense result = expenseService.addExpense(USER_ID, EMAIL, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        verify(categoryServiceClient, times(1)).getCategoriesByType(USER_ID, "EXPENSE");
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(budgetServiceClient, times(1)).updateSpentAmount(USER_ID, "Food", 500.0, EMAIL);
    }

    @Test
    void addExpense_CategoryServiceFails_ProceedsWithSave() {
        when(categoryServiceClient.getCategoriesByType(USER_ID, "EXPENSE")).thenThrow(new RuntimeException("Down"));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        Expense result = expenseService.addExpense(USER_ID, EMAIL, request);

        assertThat(result).isNotNull();
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(budgetServiceClient, times(1)).updateSpentAmount(USER_ID, "Food", 500.0, EMAIL);
    }

    @Test
    void addExpense_BudgetServiceFails_ProceedsWithSave() {
        CategoryDTO cat = new CategoryDTO();
        cat.setName("Food");
        when(categoryServiceClient.getCategoriesByType(USER_ID, "EXPENSE")).thenReturn(List.of(cat));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        doThrow(new RuntimeException("Down")).when(budgetServiceClient).updateSpentAmount(USER_ID, "Food", 500.0, EMAIL);

        Expense result = expenseService.addExpense(USER_ID, EMAIL, request);

        assertThat(result).isNotNull();
        verify(expenseRepository, times(1)).save(any(Expense.class));
        verify(budgetServiceClient, times(1)).updateSpentAmount(USER_ID, "Food", 500.0, EMAIL);
    }

    @Test
    void getExpenses_Success() {
        when(expenseRepository.findByUserId(USER_ID)).thenReturn(List.of(expense));
        List<Expense> result = expenseService.getExpenses(USER_ID);
        assertThat(result).hasSize(1);
    }

    @Test
    void deleteExpense_Success() {
        when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));
        doNothing().when(budgetServiceClient).updateSpentAmount(USER_ID, "Food", -500.0, EMAIL);
        doNothing().when(expenseRepository).delete(expense);

        expenseService.deleteExpense(10L, USER_ID, EMAIL);

        verify(expenseRepository, times(1)).delete(expense);
        verify(budgetServiceClient, times(1)).updateSpentAmount(USER_ID, "Food", -500.0, EMAIL);
    }

    @Test
    void deleteExpense_BudgetServiceFails_ProceedsWithDelete() {
        when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));
        doThrow(new RuntimeException("Down")).when(budgetServiceClient).updateSpentAmount(USER_ID, "Food", -500.0, EMAIL);
        doNothing().when(expenseRepository).delete(expense);

        expenseService.deleteExpense(10L, USER_ID, EMAIL);

        verify(expenseRepository, times(1)).delete(expense);
    }

    @Test
    void deleteExpense_NotFound_ThrowsException() {
        when(expenseRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> expenseService.deleteExpense(10L, USER_ID, EMAIL))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Expense not found");
    }

    @Test
    void deleteExpense_Unauthorized_ThrowsException() {
        expense.setUserId(99L);
        when(expenseRepository.findById(10L)).thenReturn(Optional.of(expense));

        assertThatThrownBy(() -> expenseService.deleteExpense(10L, USER_ID, EMAIL))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Unauthorized to delete this expense");
    }

    @Test
    void getAllExpenses_Success() {
        when(expenseRepository.findAll()).thenReturn(List.of(expense));
        assertThat(expenseService.getAllExpenses()).hasSize(1);
    }

    @Test
    void getExpenseCount_Success() {
        when(expenseRepository.count()).thenReturn(5L);
        assertThat(expenseService.getExpenseCount()).isEqualTo(5L);
    }
}
