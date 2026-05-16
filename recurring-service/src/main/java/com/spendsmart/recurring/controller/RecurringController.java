package com.spendsmart.recurring.controller;

import com.spendsmart.recurring.dto.RecurringRuleRequest;
import com.spendsmart.recurring.entity.RecurringRule;
import com.spendsmart.recurring.service.RecurringRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/recurring")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Recurring Management", description = "APIs for managing recurring rules")
@SecurityRequirement(name = "bearerAuth")
public class RecurringController {

    private final RecurringRuleService recurringRuleService;

    @PostMapping
    @Operation(summary = "Create a new recurring rule")
    public RecurringRule createRecurringRule(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody RecurringRuleRequest request) {

        log.info("Creating recurring rule for user: {}", email);
        return recurringRuleService.addRecurringRule(userId, email, request);
    }

    @GetMapping
    @Operation(summary = "Get all recurring rules for user")
    public List<RecurringRule> getRecurringRules(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId) {

        log.info("Retrieving recurring rules for user: {}", email);
        return recurringRuleService.getRecurringRules(userId);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update recurring rule")
    public RecurringRule updateRecurringRule(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id,
            @Valid @RequestBody RecurringRuleRequest request) {

        log.info("Updating recurring rule {} for user: {}", id, email);
        return recurringRuleService.updateRecurringRule(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete recurring rule")
    public void deleteRecurringRule(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long id) {

        log.info("Deleting recurring rule {} for user: {}", id, email);
        recurringRuleService.deleteRecurringRule(id, userId);
    }
}
