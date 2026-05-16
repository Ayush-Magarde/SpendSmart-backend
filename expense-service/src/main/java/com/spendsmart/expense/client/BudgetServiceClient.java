package com.spendsmart.expense.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "budget-service")
public interface BudgetServiceClient {
    
    @PutMapping("/api/budgets/update-spent/{userId}/{category}/{amount}")
    void updateSpentAmount(
        @PathVariable Long userId, 
        @PathVariable String category, 
        @PathVariable Double amount,
        @RequestHeader("X-User-Email") String userEmail
    );
}
