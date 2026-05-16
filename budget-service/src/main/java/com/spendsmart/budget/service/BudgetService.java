package com.spendsmart.budget.service;

import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.entity.Budget;

import java.util.List;

public interface BudgetService {

    Budget addBudget(Long userId, String email, BudgetRequest request);

    List<Budget> getBudgets(Long userId);

    Budget updateBudget(Long id, Long userId, String email, BudgetRequest request);

    void deleteBudget(Long id, Long userId);

    void updateSpentAmount(Long userId, String category, Double amount, String userEmail);
}
