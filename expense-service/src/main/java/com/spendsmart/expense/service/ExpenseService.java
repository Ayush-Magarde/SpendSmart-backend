package com.spendsmart.expense.service;

import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.entity.Expense;

import java.util.List;

public interface ExpenseService {

    Expense addExpense(Long userId, String email, ExpenseRequest request);

    List<Expense> getExpenses(Long userId);

    void deleteExpense(Long id, Long userId, String email);

    List<Expense> getAllExpenses();

    long getExpenseCount();
}
