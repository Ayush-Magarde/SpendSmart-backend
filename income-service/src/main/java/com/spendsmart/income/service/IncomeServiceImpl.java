package com.spendsmart.income.service;

import com.spendsmart.income.client.CategoryServiceClient;
import com.spendsmart.income.dto.CategoryDTO;
import com.spendsmart.income.dto.IncomeRequest;
import com.spendsmart.income.entity.Income;
import com.spendsmart.income.repository.IncomeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@org.springframework.transaction.annotation.Transactional
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final CategoryServiceClient categoryServiceClient;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Override
    public Income addIncome(Long userId, IncomeRequest request) {
        log.info("Adding income for user: {}", userId);

        // Validate category exists
        try {
            List<CategoryDTO> incomeCategories = categoryServiceClient.getCategoriesByType(userId, "INCOME");
            boolean categoryExists = incomeCategories.stream()
                    .anyMatch(cat -> cat.getName().equals(request.getCategory()));
            
            if (!categoryExists) {
                log.warn("Category '{}' does not exist for user {}, but allowing for flexibility", request.getCategory(), userId);
            }
        } catch (Exception e) {
            log.warn("Failed to validate category with Category Service: {}", e.getMessage());
            // Continue with the operation even if category validation fails
        }

        Income income = new Income();
        income.setUserId(userId);
        income.setAmount(request.getAmount());
        income.setCategory(request.getCategory());
        income.setDescription(request.getDescription());
        income.setDate(request.getDate());

        Income savedIncome = incomeRepository.save(income);
        log.info("Income added successfully with ID: {}", savedIncome.getId());

        // Publish update event for cache eviction
        publishUpdateEvent(userId);

        return savedIncome;
    }

    @Override
    public List<Income> getIncomes(Long userId) {
        return incomeRepository.findByUserId(userId);
    }

    @Override
    public void deleteIncome(Long id, Long userId) {
        log.info("Deleting income {} for user: {}", id, userId);

        Income income = incomeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Income not found"));

        if (!income.getUserId().equals(userId)) {
            throw new SecurityException("Unauthorized to delete this income");
        }

        incomeRepository.delete(income);
        log.info("Income deleted successfully with ID: {}", id);

        // Publish update event for cache eviction
        publishUpdateEvent(userId);
    }

    private void publishUpdateEvent(Long userId) {
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
            new org.springframework.transaction.support.TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        com.spendsmart.income.dto.FinancialUpdateEvent event = new com.spendsmart.income.dto.FinancialUpdateEvent(userId);
                        rabbitTemplate.convertAndSend(
                            com.spendsmart.income.config.RabbitMQConfig.FINANCIAL_UPDATE_EXCHANGE,
                            com.spendsmart.income.config.RabbitMQConfig.FINANCIAL_UPDATE_ROUTING_KEY,
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
    public List<Income> getAllIncomes() {
        return incomeRepository.findAll();
    }

    @Override
    public long getIncomeCount() {
        return incomeRepository.count();
    }
}