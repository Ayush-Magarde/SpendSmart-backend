package com.spendsmart.expense.controller;

import com.spendsmart.expense.client.CategoryServiceClient;
import com.spendsmart.expense.dto.CategoryDTO;
import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Expense Management", description = "APIs for managing expenses")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;
    private final CategoryServiceClient categoryServiceClient;

    @PostMapping
    @Operation(summary = "Create a new expense")
    public Expense addExpense(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody ExpenseRequest request) {

        log.info("Creating expense for user: {}", email);
        return expenseService.addExpense(userId, email, request);
    }

    @GetMapping
    @Operation(summary = "Get all expenses for user")
    public List<Expense> getExpenses(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Retrieving expenses for user: {}", email);
        return expenseService.getExpenses(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteExpense(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {
        log.info("Deleting expense {} for user: {}", id, email);
        expenseService.deleteExpense(id, userId, email);
    }

    // ADMIN: Get all expenses (all users)
    @GetMapping("/admin/all")
    public List<Expense> getAllExpenses(
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equals(role)) {
            throw new SecurityException("Access denied");
        }

        return expenseService.getAllExpenses();
    }

    @GetMapping("/admin/count")
    public long getExpenseCount(@RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equals(role)) {
            throw new SecurityException("Access denied");
        }

        return expenseService.getExpenseCount();
    }

    @GetMapping("/categories")
    @Operation(summary = "Get expense categories for user")
    public List<CategoryDTO> getExpenseCategories(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Retrieving expense categories for user: {}", email);
        return categoryServiceClient.getCategoriesByType(userId, "EXPENSE");
    }

}