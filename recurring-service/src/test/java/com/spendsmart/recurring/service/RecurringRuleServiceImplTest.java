package com.spendsmart.recurring.service;

import com.spendsmart.recurring.client.ExpenseServiceClient;
import com.spendsmart.recurring.client.IncomeServiceClient;
import com.spendsmart.recurring.dto.ExpenseRequest;
import com.spendsmart.recurring.dto.IncomeRequest;
import com.spendsmart.recurring.dto.RecurringRuleRequest;
import com.spendsmart.recurring.entity.RecurringRule;
import com.spendsmart.recurring.exception.RecurringException;
import com.spendsmart.recurring.repository.RecurringRuleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecurringRuleServiceImplTest {

    @Mock
    private RecurringRuleRepository recurringRuleRepository;
    @Mock
    private IncomeServiceClient incomeServiceClient;
    @Mock
    private ExpenseServiceClient expenseServiceClient;

    @InjectMocks
    private RecurringRuleServiceImpl service;

    @Test
    void addRecurringRule() {
        RecurringRuleRequest req = new RecurringRuleRequest();
        req.setName("Netflix");
        req.setAmount(10.0);
        req.setType("EXPENSE");
        req.setFrequency("MONTHLY");

        RecurringRule rule = new RecurringRule();
        rule.setId(1L);
        rule.setUserId(1L);

        when(recurringRuleRepository.save(any())).thenReturn(rule);

        RecurringRule res = service.addRecurringRule(1L, "test@test.com", req);

        assertThat(res.getId()).isEqualTo(1L);
    }

    @Test
    void getRecurringRules() {
        RecurringRule rule = new RecurringRule();
        rule.setId(1L);

        when(recurringRuleRepository.findByUserId(1L)).thenReturn(List.of(rule));

        List<RecurringRule> res = service.getRecurringRules(1L);

        assertThat(res).hasSize(1);
    }

    @Test
    void updateRecurringRule() {
        RecurringRuleRequest req = new RecurringRuleRequest();
        req.setName("Netflix2");

        RecurringRule rule = new RecurringRule();
        rule.setId(1L);
        rule.setUserId(1L);

        when(recurringRuleRepository.findById(1L)).thenReturn(Optional.of(rule));
        when(recurringRuleRepository.save(any())).thenReturn(rule);

        RecurringRule res = service.updateRecurringRule(1L, 1L, req);

        assertThat(res).isNotNull();
    }

    @Test
    void updateRecurringRule_NotFound() {
        when(recurringRuleRepository.findById(1L)).thenReturn(Optional.empty());
        
        RecurringRuleRequest req = new RecurringRuleRequest();
        assertThatThrownBy(() -> service.updateRecurringRule(1L, 1L, req))
                .isInstanceOf(RecurringException.class);
    }

    @Test
    void updateRecurringRule_Unauthorized() {
        RecurringRule rule = new RecurringRule();
        rule.setUserId(2L);
        when(recurringRuleRepository.findById(1L)).thenReturn(Optional.of(rule));

        RecurringRuleRequest req = new RecurringRuleRequest();
        assertThatThrownBy(() -> service.updateRecurringRule(1L, 1L, req))
                .isInstanceOf(SecurityException.class);
    }

    @Test
    void deleteRecurringRule() {
        RecurringRule rule = new RecurringRule();
        rule.setUserId(1L);
        when(recurringRuleRepository.findById(1L)).thenReturn(Optional.of(rule));

        service.deleteRecurringRule(1L, 1L);

        verify(recurringRuleRepository).delete(rule);
    }

    @Test
    void processRecurringRules_Expense() {
        RecurringRule rule = new RecurringRule();
        rule.setId(1L);
        rule.setUserId(1L);
        rule.setType("EXPENSE");
        rule.setFrequency("MONTHLY");
        rule.setNextDate(LocalDateTime.now().minusDays(1)); // Should trigger
        rule.setAmount(10.0);
        rule.setName("Netflix");

        when(recurringRuleRepository.findByActive(true)).thenReturn(List.of(rule));

        service.processRecurringRules();

        verify(expenseServiceClient).createExpense(any(), eq(1L), any(ExpenseRequest.class));
        verify(recurringRuleRepository).save(rule);
    }

    @Test
    void processRecurringRules_Income() {
        RecurringRule rule = new RecurringRule();
        rule.setId(1L);
        rule.setUserId(1L);
        rule.setType("INCOME");
        rule.setFrequency("DAILY");
        rule.setNextDate(LocalDateTime.now().minusDays(1)); // Should trigger
        rule.setAmount(10.0);
        rule.setName("Salary");

        when(recurringRuleRepository.findByActive(true)).thenReturn(List.of(rule));

        service.processRecurringRules();

        verify(incomeServiceClient).createIncome(any(), eq(1L), any(IncomeRequest.class));
        verify(recurringRuleRepository).save(rule);
    }

    @Test
    void processRecurringRules_Weekly() {
        RecurringRule rule = new RecurringRule();
        rule.setId(1L);
        rule.setUserId(1L);
        rule.setType("EXPENSE");
        rule.setFrequency("WEEKLY");
        rule.setNextDate(LocalDateTime.now().minusDays(1)); 
        rule.setAmount(10.0);
        rule.setName("Weekly Bill");

        when(recurringRuleRepository.findByActive(true)).thenReturn(List.of(rule));

        service.processRecurringRules();

        verify(expenseServiceClient).createExpense(any(), eq(1L), any(ExpenseRequest.class));
        verify(recurringRuleRepository).save(rule);
    }

    @Test
    void processRecurringRules_Yearly() {
        RecurringRule rule = new RecurringRule();
        rule.setId(1L);
        rule.setUserId(1L);
        rule.setType("INCOME");
        rule.setFrequency("YEARLY");
        rule.setNextDate(LocalDateTime.now().minusDays(1)); 
        rule.setAmount(10.0);
        rule.setName("Yearly Bonus");

        when(recurringRuleRepository.findByActive(true)).thenReturn(List.of(rule));

        service.processRecurringRules();

        verify(incomeServiceClient).createIncome(any(), eq(1L), any(IncomeRequest.class));
        verify(recurringRuleRepository).save(rule);
    }

    @Test
    void processRecurringRules_UnknownFrequency() {
        RecurringRule rule = new RecurringRule();
        rule.setId(1L);
        rule.setUserId(1L);
        rule.setType("EXPENSE");
        rule.setFrequency("HOURLY"); // Unknown
        rule.setNextDate(LocalDateTime.now().minusDays(1)); 
        rule.setAmount(10.0);
        rule.setName("Hourly Bill");

        when(recurringRuleRepository.findByActive(true)).thenReturn(List.of(rule));

        service.processRecurringRules();

        verify(recurringRuleRepository).save(rule);
    }
}
