package com.spendsmart.summary.service;

import com.spendsmart.summary.client.ExpenseServiceClient;
import com.spendsmart.summary.client.IncomeServiceClient;
import com.spendsmart.summary.dto.ExpenseDTO;
import com.spendsmart.summary.dto.IncomeDTO;
import com.spendsmart.summary.dto.SummaryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummaryServiceImplTest {

    @Mock
    private IncomeServiceClient incomeServiceClient;

    @Mock
    private ExpenseServiceClient expenseServiceClient;

    @InjectMocks
    private SummaryServiceImpl summaryService;

    private String email = "test@example.com";
    private Long userId = 1L;

    @Test
    void getSummary_returnsCorrectCalculations() {
        // Arrange
        IncomeDTO inc1 = new IncomeDTO(); inc1.setAmount(5000.0);
        IncomeDTO inc2 = new IncomeDTO(); inc2.setAmount(2000.0);
        IncomeDTO inc3 = new IncomeDTO(); // Null amount
        
        ExpenseDTO exp1 = new ExpenseDTO(); exp1.setAmount(1000.0); exp1.setCategory("Food");
        ExpenseDTO exp2 = new ExpenseDTO(); exp2.setAmount(500.0); exp2.setCategory("Food");
        ExpenseDTO exp3 = new ExpenseDTO(); exp3.setAmount(1500.0); exp3.setCategory("Transport");
        ExpenseDTO exp4 = new ExpenseDTO(); // Null amount
        ExpenseDTO exp5 = new ExpenseDTO(); exp5.setAmount(200.0); // Null category

        when(incomeServiceClient.getIncomes(email, userId)).thenReturn(List.of(inc1, inc2, inc3));
        when(expenseServiceClient.getExpenses(email, userId)).thenReturn(List.of(exp1, exp2, exp3, exp4, exp5));

        // Act
        SummaryResponse response = summaryService.getSummary(email, userId);

        // Assert
        assertThat(response.getTotalIncome()).isEqualTo(7000.0);
        assertThat(response.getTotalExpense()).isEqualTo(3200.0);
        assertThat(response.getBalance()).isEqualTo(3800.0);
        
        assertThat(response.getCategoryBreakdown())
                .containsEntry("Food", 1500.0)
                .containsEntry("Transport", 1500.0)
                .doesNotContainKey(null);
                
        assertThat(response.getMonthlyComparison()).hasSize(1);
        assertThat(response.getMonthlyComparison().get(0))
                .containsEntry("month", "Current")
                .containsEntry("income", 7000.0)
                .containsEntry("expense", 3200.0);
    }

    @Test
    void getSummary_handlesNullResponsesFromClients() {
        // Arrange
        when(incomeServiceClient.getIncomes(email, userId)).thenReturn(null);
        when(expenseServiceClient.getExpenses(email, userId)).thenReturn(null);

        // Act
        SummaryResponse response = summaryService.getSummary(email, userId);

        // Assert
        assertThat(response.getTotalIncome()).isEqualTo(0.0);
        assertThat(response.getTotalExpense()).isEqualTo(0.0);
        assertThat(response.getBalance()).isEqualTo(0.0);
        assertThat(response.getCategoryBreakdown()).isEmpty();
    }
}
