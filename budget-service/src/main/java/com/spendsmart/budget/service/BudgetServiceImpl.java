package com.spendsmart.budget.service;

import com.spendsmart.budget.client.NotificationServiceClient;
import com.spendsmart.budget.dto.BudgetRequest;
import com.spendsmart.budget.dto.NotificationRequest;
import com.spendsmart.budget.entity.Budget;
import com.spendsmart.budget.exception.BudgetException;
import com.spendsmart.budget.repository.BudgetRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final NotificationServiceClient notificationServiceClient;
    private final com.spendsmart.budget.client.ExpenseServiceClient expenseServiceClient;

    @Override
    @Transactional
    public Budget addBudget(Long userId, String email, BudgetRequest request) {
        log.info("Adding budget for user: {}", userId);

        // Check for duplicate category
        if (budgetRepository.existsByUserIdAndCategoryIgnoreCase(userId, request.getCategory())) {
            throw new BudgetException("A budget for category '" + request.getCategory() + "' already exists. Please update the existing budget instead.");
        }

        Budget budget = new Budget();
        budget.setUserId(userId);
        budget.setName(request.getName());
        budget.setAmount(request.getAmount());
        budget.setCategory(request.getCategory());
        budget.setAlertThreshold(request.getAlertThreshold() != null ? request.getAlertThreshold() : request.getAmount() * 0.8);
        budget.setSpent(0.0);
        budget.setCreatedAt(java.time.LocalDateTime.now());
        budget.setUpdatedAt(java.time.LocalDateTime.now());
        budget.setActive(true);

        // Sync initial spent amount from expense service
        syncSpentAmount(budget, userId, email);

        Budget savedBudget = budgetRepository.save(budget);
        log.info("Budget added successfully with ID: {}", savedBudget.getId());

        return savedBudget;
    }

    @Override
    public List<Budget> getBudgets(Long userId) {
        log.info("Retrieving budgets for user: {}", userId);
        return budgetRepository.findByUserId(userId);
    }

    @Override
    @Transactional
    public Budget updateBudget(Long id, Long userId, String email, BudgetRequest request) {
        log.info("Updating budget {} for user: {}", id, userId);

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetException("Budget not found"));

        if (!budget.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized to update this budget");
        }

        // If category is being changed, check for duplicates
        if (!budget.getCategory().equalsIgnoreCase(request.getCategory()) &&
                budgetRepository.existsByUserIdAndCategoryIgnoreCase(userId, request.getCategory())) {
            throw new BudgetException("A budget for category '" + request.getCategory() + "' already exists.");
        }

        budget.setName(request.getName());
        budget.setAmount(request.getAmount());
        budget.setCategory(request.getCategory());
        budget.setAlertThreshold(request.getAlertThreshold() != null ? request.getAlertThreshold() : budget.getAlertThreshold());
        budget.setUpdatedAt(java.time.LocalDateTime.now());

        // Re-sync spent amount as category or amount might have changed
        syncSpentAmount(budget, userId, email);

        Budget updatedBudget = budgetRepository.save(budget);
        log.info("Budget updated successfully with ID: {}", id);

        return updatedBudget;
    }

    private void syncSpentAmount(Budget budget, Long userId, String email) {
        try {
            log.info("Syncing spent amount for category: {}", budget.getCategory());
            List<com.spendsmart.budget.dto.ExpenseDTO> expenses = expenseServiceClient.getExpenses(email, userId);
            double totalSpent = expenses.stream()
                    .filter(e -> budget.getCategory().equalsIgnoreCase(e.getCategory()))
                    .mapToDouble(com.spendsmart.budget.dto.ExpenseDTO::getAmount)
                    .sum();
            
            budget.setSpent(totalSpent);
            log.info("Synced spent amount for {}: ₹{}", budget.getCategory(), totalSpent);

            // Check if threshold exceeded during sync
            if (budget.getSpent() > budget.getAlertThreshold()) {
                sendThresholdNotification(budget, email, userId);
            }
        } catch (Exception e) {
            log.warn("Failed to sync spent amount from expense service: {}", e.getMessage());
        }
    }

    private void sendThresholdNotification(Budget budget, String email, Long userId) {
        try {
            NotificationRequest notificationRequest = new NotificationRequest();
            notificationRequest.setTitle("Budget Alert");
            notificationRequest.setMessage("Budget '" + budget.getName() + "' has exceeded its threshold! Spent: ₹" + budget.getSpent() + " of ₹" + budget.getAmount());
            notificationRequest.setType("WARNING");
            notificationRequest.setCategory("BUDGET");
            notificationRequest.setIsRead(false);
            
            notificationServiceClient.createNotification(email, userId, notificationRequest);
            log.info("Budget threshold notification sent for budget ID: {}", budget.getId());
        } catch (Exception e) {
            log.warn("Failed to send budget alert notification: {}", e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteBudget(Long id, Long userId) {
        log.info("Deleting budget {} for user: {}", id, userId);

        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new BudgetException("Budget not found"));

        if (!budget.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized to delete this budget");
        }

        budgetRepository.delete(budget);
        log.info("Budget deleted successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void updateSpentAmount(Long userId, String category, Double amount, String userEmail) {
        log.info("Updating spent amount for user: {}, category: {}, amount: {}", userId, category, amount);

        List<Budget> budgets = budgetRepository.findByUserId(userId);
        
        // Iterate backwards to find the most recently created budget for this category,
        // which matches the exact budget the frontend UI displays.
        for (int i = budgets.size() - 1; i >= 0; i--) {
            Budget budget = budgets.get(i);
            if (category.equals(budget.getCategory())) {
                budget.setSpent(budget.getSpent() + amount);
                budget.setUpdatedAt(java.time.LocalDateTime.now());
                budgetRepository.save(budget);
                log.info("Updated spent amount for budget ID: {}", budget.getId());
                
                // Check if threshold exceeded
                if (budget.getSpent() > budget.getAlertThreshold()) {
                    log.info("Budget threshold exceeded for budget ID: {}", budget.getId());
                    
                    // Create notification
                    try {
                        NotificationRequest notificationRequest = new NotificationRequest();
                        notificationRequest.setTitle("Budget Alert");
                        notificationRequest.setMessage("Budget '" + budget.getName() + "' has exceeded its threshold! Spent: ₹" + budget.getSpent() + " of ₹" + budget.getAmount());
                        notificationRequest.setType("WARNING");
                        notificationRequest.setCategory("BUDGET");
                        notificationRequest.setIsRead(false);
                        
                        notificationServiceClient.createNotification(userEmail, userId, notificationRequest);
                        log.info("Budget alert notification sent for budget ID: {}", budget.getId());
                    } catch (Exception e) {
                        log.warn("Failed to send budget alert notification: {}", e.getMessage());
                    }
                }
                break; // Stop after updating the active one
            }
        }
    }
}
