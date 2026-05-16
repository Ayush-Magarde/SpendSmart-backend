package com.spendsmart.income.service;

import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.entity.Income;

import java.util.List;

public interface IncomeService {

    Income addIncome(Long userId, IncomeRequest request);

    List<Income> getIncomes(Long userId);

    void deleteIncome(Long id, Long userId);

    List<Income> getAllIncomes();

    long getIncomeCount();
}