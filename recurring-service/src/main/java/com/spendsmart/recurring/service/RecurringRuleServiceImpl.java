package com.spendsmart.recurring.service;

import com.spendsmart.recurring.client.ExpenseServiceClient;
import com.spendsmart.recurring.client.IncomeServiceClient;
import com.spendsmart.recurring.dto.ExpenseRequest;
import com.spendsmart.recurring.dto.IncomeRequest;
import com.spendsmart.recurring.dto.RecurringRuleRequest;
import com.spendsmart.recurring.entity.RecurringRule;
import com.spendsmart.recurring.exception.RecurringException;
import com.spendsmart.recurring.repository.RecurringRuleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringRuleServiceImpl implements RecurringRuleService {

    private final RecurringRuleRepository recurringRuleRepository;
    private final IncomeServiceClient incomeServiceClient;
    private final ExpenseServiceClient expenseServiceClient;

    @Override
    @Transactional
    public RecurringRule addRecurringRule(Long userId, String email, RecurringRuleRequest request) {
        log.info("Adding recurring rule for user: {}", email);

        RecurringRule recurringRule = new RecurringRule();
        recurringRule.setUserId(userId);
        recurringRule.setUserEmail(email); // Store real user email for background processing
        recurringRule.setName(request.getName());
        recurringRule.setType(request.getType());
        recurringRule.setAmount(request.getAmount());
        recurringRule.setFrequency(request.getFrequency());
        recurringRule.setStartDate(java.time.LocalDateTime.now());
        recurringRule.setNextDate(java.time.LocalDateTime.now());
        recurringRule.setActive(true);
        recurringRule.setCreatedAt(java.time.LocalDateTime.now());

        RecurringRule savedRule = recurringRuleRepository.save(recurringRule);
        log.info("Recurring rule added successfully with ID: {}", savedRule.getId());

        return savedRule;
    }

    @Override
    public List<RecurringRule> getRecurringRules(Long userId) {
        log.info("Retrieving recurring rules for user: {}", userId);
        return recurringRuleRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public RecurringRule updateRecurringRule(Long id, Long userId, RecurringRuleRequest request) {
        log.info("Updating recurring rule {} for user: {}", id, userId);

        RecurringRule recurringRule = recurringRuleRepository.findById(id)
                .orElseThrow(() -> new RecurringException("Recurring rule not found"));
        if (!recurringRule.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized to update this recurring rule");
        }

        recurringRule.setName(request.getName());
        recurringRule.setType(request.getType());
        recurringRule.setAmount(request.getAmount());
        recurringRule.setFrequency(request.getFrequency());

        RecurringRule updatedRule = recurringRuleRepository.save(recurringRule);
        log.info("Recurring rule updated successfully with ID: {}", id);

        return updatedRule;
    }

    @Override
    @Transactional
    public void deleteRecurringRule(Long id, Long userId) {
        log.info("Deleting recurring rule {} for user: {}", id, userId);

        RecurringRule recurringRule = recurringRuleRepository.findById(id)
                .orElseThrow(() -> new RecurringException("Recurring rule not found"));
        if (!recurringRule.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized to delete this recurring rule");
        }

        recurringRuleRepository.delete(recurringRule);
        log.info("Recurring rule deleted successfully with ID: {}", id);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void processRecurringRules() {
        log.info("Processing recurring rules - Started at: {}", LocalDateTime.now());

        List<RecurringRule> activeRules = recurringRuleRepository.findByActive(true);
        
        for (RecurringRule rule : activeRules) {
            try {
                if (rule.getNextDate().isBefore(LocalDateTime.now()) || rule.getNextDate().isEqual(LocalDateTime.now())) {
                    log.info("Processing recurring rule ID: {} for user: {}", rule.getId(), rule.getUserId());
                    
                    // Create transaction based on type
                    if ("INCOME".equals(rule.getType())) {
                        createIncomeTransaction(rule, rule.getUserEmail(), rule.getUserId());
                    } else if ("EXPENSE".equals(rule.getType())) {
                        createExpenseTransaction(rule, rule.getUserEmail(), rule.getUserId());
                    }
                    
                    // Update next date based on frequency
                    updateNextDate(rule);
                    
                    recurringRuleRepository.save(rule);
                    log.info("Recurring rule {} processed successfully", rule.getId());
                }
            } catch (Exception e) {
                log.error("Failed to process recurring rule {}: {}", rule.getId(), e.getMessage());
            }
        }
        
        log.info("Processing recurring rules - Completed at: {}", LocalDateTime.now());
    }

    private void createIncomeTransaction(RecurringRule rule, String userEmail, Long userId) {
        IncomeRequest incomeRequest = new IncomeRequest();
        incomeRequest.setAmount(rule.getAmount());
        incomeRequest.setCategory(rule.getName());
        incomeRequest.setDescription("Auto-generated from recurring rule: " + rule.getName());
        incomeRequest.setDate(LocalDate.now());
        
        incomeServiceClient.createIncome(userEmail, userId, incomeRequest);
        log.info("Created income transaction for rule ID: {}", rule.getId());
    }

    private void createExpenseTransaction(RecurringRule rule, String userEmail, Long userId) {
        ExpenseRequest expenseRequest = new ExpenseRequest();
        expenseRequest.setAmount(rule.getAmount());
        expenseRequest.setCategory("RECURRING");
        expenseRequest.setDescription("Auto-generated from recurring rule: " + rule.getName());
        expenseRequest.setPaymentMethod("AUTO");
        expenseRequest.setDate(LocalDate.now());
        
        expenseServiceClient.createExpense(userEmail, userId, expenseRequest);
        log.info("Created expense transaction for rule ID: {}", rule.getId());
    }

    private void updateNextDate(RecurringRule rule) {
        LocalDateTime nextDate = rule.getNextDate();
        
        switch (rule.getFrequency().toUpperCase()) {
            case "DAILY":
                nextDate = nextDate.plusDays(1);
                break;
            case "WEEKLY":
                nextDate = nextDate.plusWeeks(1);
                break;
            case "MONTHLY":
                nextDate = nextDate.plusMonths(1);
                break;
            case "YEARLY":
                nextDate = nextDate.plusYears(1);
                break;
            default:
                log.warn("Unknown frequency: {} for rule ID: {}", rule.getFrequency(), rule.getId());
                return;
        }
        
        rule.setNextDate(nextDate);
        log.info("Updated next date to {} for rule ID: {}", nextDate, rule.getId());
    }
}
