package com.spendsmart.expense.service;

import com.spendsmart.expense.client.BudgetServiceClient;
import com.spendsmart.expense.client.CategoryServiceClient;
import com.spendsmart.expense.dto.CategoryDTO;
import com.spendsmart.expense.dto.ExpenseRequest;
import com.spendsmart.expense.entity.Expense;
import com.spendsmart.expense.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.transaction.annotation.Transactional
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final BudgetServiceClient budgetServiceClient;
    private final CategoryServiceClient categoryServiceClient;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Override
    public Expense addExpense(Long userId, String email, ExpenseRequest request) {
        log.info("Adding expense for user: {}", userId);

        // Validate category exists
        try {
            List<CategoryDTO> expenseCategories = categoryServiceClient.getCategoriesByType(userId, "EXPENSE");
            boolean categoryExists = expenseCategories.stream()
                    .anyMatch(cat -> cat.getName().equals(request.getCategory()));
            
            if (!categoryExists) {
                log.warn("Category '{}' does not exist for user {}, but allowing for flexibility", request.getCategory(), userId);
            }
        } catch (Exception e) {
            log.warn("Failed to validate category with Category Service: {}", e.getMessage());
            // Continue with the operation even if category validation fails
        }

        Expense expense = new Expense();
        expense.setUserId(userId);
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDescription(request.getDescription());
        expense.setPaymentMethod(request.getPaymentMethod());
        expense.setDate(request.getDate());

        Expense savedExpense = expenseRepository.save(expense);
        log.info("Expense added successfully with ID: {}", savedExpense.getId());

        // Update budget spent amount
        try {
            budgetServiceClient.updateSpentAmount(userId, request.getCategory(), request.getAmount(), email);
            log.info("Budget spent amount updated for category: {}", request.getCategory());
        } catch (Exception e) {
            log.warn("Failed to update budget spent amount: {}", e.getMessage());
        }

        // Publish update event for cache eviction
        publishUpdateEvent(userId);

        return savedExpense;
    }

    @Override
    public List<Expense> getExpenses(Long userId) {
        return expenseRepository.findByUserId(userId);
    }
    
    @Override
    public void deleteExpense(Long id, Long userId, String email) {
        log.info("Deleting expense {} for user: {}", id, userId);
        
        Expense expense = expenseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Expense not found"));

        if (!expense.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized to delete this expense");
        }

        // Before deleting, subtract amount from budget
        try {
            log.info("Subtracting amount {} from budget category: {}", expense.getAmount(), expense.getCategory());
            budgetServiceClient.updateSpentAmount(userId, expense.getCategory(), -expense.getAmount(), email);
        } catch (Exception e) {
            log.warn("Failed to update budget (decrement) for deleted expense: {}", e.getMessage());
        }

        expenseRepository.delete(expense);
        log.info("Expense deleted successfully with ID: {}", id);

        // Publish update event for cache eviction
        publishUpdateEvent(userId);
    }

    private void publishUpdateEvent(Long userId) {
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        com.spendsmart.expense.dto.FinancialUpdateEvent event = new com.spendsmart.expense.dto.FinancialUpdateEvent(userId);
                        rabbitTemplate.convertAndSend(
                            com.spendsmart.expense.config.RabbitMQConfig.FINANCIAL_UPDATE_EXCHANGE,
                            com.spendsmart.expense.config.RabbitMQConfig.FINANCIAL_UPDATE_ROUTING_KEY,
                            event
                        );
                        log.info("Published financial update event for user: {} after transaction commit", userId);
                    } catch (Exception e) {
                        log.error("Failed to publish financial update event: {}", e.getMessage());
                    }
                }
            }
        );
    }

    @Override
    public List<Expense> getAllExpenses() {
        return expenseRepository.findAll();
    }

    @Override
    public long getExpenseCount() {
        return expenseRepository.count();
    }

}