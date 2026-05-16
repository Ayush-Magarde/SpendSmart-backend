package com.spendsmart.income.controller;

import com.spendsmart.income.client.CategoryServiceClient;
import com.spendsmart.income.dto.CategoryDTO;
import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.service.IncomeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/incomes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Income Management", description = "APIs for managing incomes")
@SecurityRequirement(name = "bearerAuth")
public class IncomeController {

    private final IncomeService incomeService;
    private final CategoryServiceClient categoryServiceClient;

    @PostMapping
    @Operation(summary = "Create a new income")
    public Income addIncome(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody IncomeRequest request) {

        log.info("Creating income for user: {}", email);
        return incomeService.addIncome(userId, request);
    }

    @GetMapping
    @Operation(summary = "Get all incomes for user")
    public List<Income> getIncomes(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Retrieving incomes for user: {}", email);
        return incomeService.getIncomes(userId);
    }

    @DeleteMapping("/{id}")
    public void deleteIncome(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {

        incomeService.deleteIncome(id, userId);
    }

    // 👨‍💼 ADMIN: Get all incomes
    @GetMapping("/admin/all")
    public List<Income> getAllIncomes(
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equals(role)) {
            throw new SecurityException("Access denied");
        }

        return incomeService.getAllIncomes();
    }

    // ADMIN: Count
    @GetMapping("/admin/count")
    public long getIncomeCount(
            @RequestHeader("X-User-Role") String role) {

        if (!"ADMIN".equals(role)) {
            throw new SecurityException("Access denied");
        }

        return incomeService.getIncomeCount();
    }

    @GetMapping("/categories")
    @Operation(summary = "Get income categories for user")
    public List<CategoryDTO> getIncomeCategories(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Retrieving income categories for user: {}", email);
        return categoryServiceClient.getCategoriesByType(userId, "INCOME");
    }
}