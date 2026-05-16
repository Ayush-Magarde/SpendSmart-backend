package com.spendsmart.budget.controller;

import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.service.BudgetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Budget Management", description = "APIs for managing budgets")
@SecurityRequirement(name = "bearerAuth")
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    @Operation(summary = "Create a new budget")
    public Budget createBudget(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody BudgetRequest request) {

        log.info("Creating budget for user: {}", email);
        return budgetService.addBudget(userId, email, request);
    }

    @GetMapping
    @Operation(summary = "Get all budgets for user")
    public List<Budget> getBudgets(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Retrieving budgets for user: {}", email);
        return budgetService.getBudgets(userId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update budget")
    public Budget updateBudget(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody BudgetRequest request) {

        log.info("Updating budget {} for user: {}", id, email);
        return budgetService.updateBudget(id, userId, email, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete budget")
    public void deleteBudget(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {

        log.info("Deleting budget {} for user: {}", id, email);
        budgetService.deleteBudget(id, userId);
    }

    @PutMapping("/update-spent/{userId}/{category}/{amount}")
    @Operation(summary = "Update spent amount for budget")
    public void updateSpentAmount(
            @PathVariable Long userId,
            @PathVariable String category,
            @PathVariable Double amount,
            @RequestHeader("X-User-Email") String email) {

        log.info("Updating spent amount for user: {}, category: {}, amount: {}", userId, category, amount);
        budgetService.updateSpentAmount(userId, category, amount, email);
    }
}
