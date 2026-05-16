package com.spendsmart.budget.service;

import com.spendsmart.budget.client.ExpenseServiceClient;
import com.spendsmart.budget.client.NotificationServiceClient;
import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.dto.ExpenseDTO;
import com.spendsmart.budget.dto.NotificationRequest;
import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.exception.BudgetException;
import com.spendsmart.budget.repository.BudgetRepository;
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
class BudgetServiceImplTest {

    @Mock
    private BudgetRepository budgetRepository;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Mock
    private ExpenseServiceClient expenseServiceClient;

    @InjectMocks
    private BudgetServiceImpl budgetService;

    private BudgetRequest request;
    private Budget budget;
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
    void addBudget_DuplicateCategory_ThrowsException() {
        when(budgetRepository.existsByUserIdAndCategoryIgnoreCase(USER_ID, "Food")).thenReturn(true);

        assertThatThrownBy(() -> budgetService.addBudget(USER_ID, EMAIL, request))
                .isInstanceOf(BudgetException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void addBudget_Success_NoAlert() {
        when(budgetRepository.existsByUserIdAndCategoryIgnoreCase(USER_ID, "Food")).thenReturn(false);
        when(expenseServiceClient.getExpenses(EMAIL, USER_ID)).thenReturn(List.of());
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Budget result = budgetService.addBudget(USER_ID, EMAIL, request);

        assertThat(result).isNotNull();
        verify(budgetRepository, times(1)).save(any(Budget.class));
        verify(notificationServiceClient, never()).createNotification(anyString(), anyLong(), any());
    }

    @Test
    void addBudget_Success_WithAlert() {
        when(budgetRepository.existsByUserIdAndCategoryIgnoreCase(USER_ID, "Food")).thenReturn(false);
        
        ExpenseDTO exp = new ExpenseDTO();
        exp.setCategory("Food");
        exp.setAmount(900.0); // Over the 800.0 threshold
        when(expenseServiceClient.getExpenses(EMAIL, USER_ID)).thenReturn(List.of(exp));
        
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        budgetService.addBudget(USER_ID, EMAIL, request);

        verify(notificationServiceClient, times(1)).createNotification(eq(EMAIL), eq(USER_ID), any(NotificationRequest.class));
    }

    @Test
    void addBudget_ExpenseServiceFails_ProceedsWithSave() {
        when(budgetRepository.existsByUserIdAndCategoryIgnoreCase(USER_ID, "Food")).thenReturn(false);
        when(expenseServiceClient.getExpenses(EMAIL, USER_ID)).thenThrow(new RuntimeException("Down"));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        budgetService.addBudget(USER_ID, EMAIL, request);

        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void getBudgets_Success() {
        when(budgetRepository.findByUserId(USER_ID)).thenReturn(List.of(budget));
        assertThat(budgetService.getBudgets(USER_ID)).hasSize(1);
    }

    @Test
    void updateBudget_Success() {
        request.setCategory("Dining");
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));
        when(budgetRepository.existsByUserIdAndCategoryIgnoreCase(USER_ID, "Dining")).thenReturn(false);
        when(expenseServiceClient.getExpenses(EMAIL, USER_ID)).thenReturn(List.of());
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        Budget result = budgetService.updateBudget(10L, USER_ID, EMAIL, request);

        assertThat(result).isNotNull();
        verify(budgetRepository, times(1)).save(any(Budget.class));
    }

    @Test
    void updateBudget_DuplicateCategory_ThrowsException() {
        request.setCategory("Dining");
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));
        when(budgetRepository.existsByUserIdAndCategoryIgnoreCase(USER_ID, "Dining")).thenReturn(true);

        assertThatThrownBy(() -> budgetService.updateBudget(10L, USER_ID, EMAIL, request))
                .isInstanceOf(BudgetException.class);
    }

    @Test
    void updateBudget_Unauthorized_ThrowsException() {
        budget.setUserId(99L);
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));

        assertThatThrownBy(() -> budgetService.updateBudget(10L, USER_ID, EMAIL, request))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void deleteBudget_Success() {
        when(budgetRepository.findById(10L)).thenReturn(Optional.of(budget));
        doNothing().when(budgetRepository).delete(budget);

        budgetService.deleteBudget(10L, USER_ID);

        verify(budgetRepository, times(1)).delete(budget);
    }

    @Test
    void updateSpentAmount_Success_NoAlert() {
        when(budgetRepository.findByUserId(USER_ID)).thenReturn(List.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        budgetService.updateSpentAmount(USER_ID, "Food", 500.0, EMAIL);

        verify(budgetRepository, times(1)).save(any(Budget.class));
        verify(notificationServiceClient, never()).createNotification(anyString(), anyLong(), any());
    }

    @Test
    void updateSpentAmount_Success_WithAlert() {
        when(budgetRepository.findByUserId(USER_ID)).thenReturn(List.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);

        budgetService.updateSpentAmount(USER_ID, "Food", 900.0, EMAIL);

        verify(budgetRepository, times(1)).save(any(Budget.class));
        verify(notificationServiceClient, times(1)).createNotification(eq(EMAIL), eq(USER_ID), any(NotificationRequest.class));
    }

    @Test
    void updateSpentAmount_NotificationFails_ProceedsWithSave() {
        when(budgetRepository.findByUserId(USER_ID)).thenReturn(List.of(budget));
        when(budgetRepository.save(any(Budget.class))).thenReturn(budget);
        doThrow(new RuntimeException("Down")).when(notificationServiceClient).createNotification(anyString(), anyLong(), any());

        budgetService.updateSpentAmount(USER_ID, "Food", 900.0, EMAIL);

        verify(budgetRepository, times(1)).save(any(Budget.class));
    }
}
