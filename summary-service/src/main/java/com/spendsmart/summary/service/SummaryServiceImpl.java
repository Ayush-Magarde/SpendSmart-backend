package com.spendsmart.summary.service;

import com.spendsmart.summary.dto.ExpenseDTO;
import com.spendsmart.summary.dto.IncomeDTO;
import com.spendsmart.summary.client.ExpenseServiceClient;
import com.spendsmart.summary.client.IncomeServiceClient;
import com.spendsmart.summary.dto.SummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {

    private final IncomeServiceClient incomeServiceClient;
    private final ExpenseServiceClient expenseServiceClient;

    @Override
    @Cacheable(value = "dashboard_summary", key = "#userId")
    public SummaryResponse getSummary(String email, Long userId) {

        // Call Income Service using Feign
        List<IncomeDTO> incomes = incomeServiceClient.getIncomes(email, userId);

        // Call Expense Service using Feign
        List<ExpenseDTO> expenses = expenseServiceClient.getExpenses(email, userId);

        // Null safety
        if (incomes == null) incomes = List.of();
        if (expenses == null) expenses = List.of();

        // Calculation
        double totalIncome = incomes.stream()
                .filter(i -> i.getAmount() != null)
                .mapToDouble(IncomeDTO::getAmount)
                .sum();

        double totalExpense = expenses.stream()
                .filter(e -> e.getAmount() != null)
                .mapToDouble(ExpenseDTO::getAmount)
                .sum();

        double balance = totalIncome - totalExpense;

        // Category breakdown
        Map<String, Double> categoryBreakdown = expenses.stream()
                .filter(e -> e.getCategory() != null)
                .filter(e -> e.getAmount() != null)
                .collect(Collectors.groupingBy(
                        ExpenseDTO::getCategory,
                        Collectors.summingDouble(ExpenseDTO::getAmount)
                ));

        // Monthly comparison (simplified - just current month)
        List<Map<String, Object>> monthlyComparison = List.of(
                Map.of("month", "Current", "income", totalIncome, "expense", totalExpense)
        );

        return new SummaryResponse(totalIncome, totalExpense, balance, categoryBreakdown, monthlyComparison);
    }
}