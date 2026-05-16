package com.spendsmart.income.service;

import com.spendsmart.income.client.CategoryServiceClient;
import com.spendsmart.income.dto.CategoryDTO;
import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.repository.IncomeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncomeServiceImplTest {

    @Mock
    private IncomeRepository incomeRepository;

    @Mock
    private CategoryServiceClient categoryServiceClient;

    @InjectMocks
    private IncomeServiceImpl incomeService;

    private IncomeRequest request;
    private Income income;
    private final Long USER_ID = 1L;

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
    void addIncome_CategoryExists_Success() {
        CategoryDTO cat = new CategoryDTO();
        cat.setName("Salary");
        when(categoryServiceClient.getCategoriesByType(USER_ID, "INCOME")).thenReturn(List.of(cat));
        when(incomeRepository.save(any(Income.class))).thenReturn(income);

        Income result = incomeService.addIncome(USER_ID, request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getAmount()).isEqualTo(1000.0);
        verify(categoryServiceClient, times(1)).getCategoriesByType(USER_ID, "INCOME");
        verify(incomeRepository, times(1)).save(any(Income.class));
    }

    @Test
    void addIncome_CategoryDoesNotExist_AllowsFlexibility() {
        CategoryDTO cat = new CategoryDTO();
        cat.setName("Freelance");
        when(categoryServiceClient.getCategoriesByType(USER_ID, "INCOME")).thenReturn(List.of(cat));
        when(incomeRepository.save(any(Income.class))).thenReturn(income);

        Income result = incomeService.addIncome(USER_ID, request);

        assertThat(result).isNotNull();
        verify(incomeRepository, times(1)).save(any(Income.class));
    }

    @Test
    void addIncome_CategoryServiceFails_ProceedsWithSave() {
        when(categoryServiceClient.getCategoriesByType(USER_ID, "INCOME"))
                .thenThrow(new RuntimeException("Service Down"));
        when(incomeRepository.save(any(Income.class))).thenReturn(income);

        Income result = incomeService.addIncome(USER_ID, request);

        assertThat(result).isNotNull();
        verify(incomeRepository, times(1)).save(any(Income.class));
    }

    @Test
    void getIncomes_Success() {
        when(incomeRepository.findByUserId(USER_ID)).thenReturn(List.of(income));

        List<Income> result = incomeService.getIncomes(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);
        verify(incomeRepository, times(1)).findByUserId(USER_ID);
    }

    @Test
    void deleteIncome_Success() {
        when(incomeRepository.findById(10L)).thenReturn(Optional.of(income));
        doNothing().when(incomeRepository).delete(income);

        incomeService.deleteIncome(10L, USER_ID);

        verify(incomeRepository, times(1)).findById(10L);
        verify(incomeRepository, times(1)).delete(income);
    }

    @Test
    void deleteIncome_NotFound_ThrowsException() {
        when(incomeRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> incomeService.deleteIncome(10L, USER_ID))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Income not found");

        verify(incomeRepository, never()).delete(any());
    }

    @Test
    void deleteIncome_Unauthorized_ThrowsSecurityException() {
        income.setUserId(2L); // Different user
        when(incomeRepository.findById(10L)).thenReturn(Optional.of(income));

        assertThatThrownBy(() -> incomeService.deleteIncome(10L, USER_ID))
                .isInstanceOf(SecurityException.class)
                .hasMessage("Unauthorized to delete this income");

        verify(incomeRepository, never()).delete(any());
    }

    @Test
    void getAllIncomes_Success() {
        when(incomeRepository.findAll()).thenReturn(List.of(income));

        List<Income> result = incomeService.getAllIncomes();

        assertThat(result).hasSize(1);
        verify(incomeRepository, times(1)).findAll();
    }

    @Test
    void getIncomeCount_Success() {
        when(incomeRepository.count()).thenReturn(5L);

        long result = incomeService.getIncomeCount();

        assertThat(result).isEqualTo(5L);
        verify(incomeRepository, times(1)).count();
    }
}
