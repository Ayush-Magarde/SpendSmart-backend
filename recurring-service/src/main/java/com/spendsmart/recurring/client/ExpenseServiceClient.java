package com.spendsmart.recurring.client;

import com.spendsmart.recurring.dto.ExpenseRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "expense-service")
public interface ExpenseServiceClient {
    
    @PostMapping("/api/expenses")
    void createExpense(
        @RequestHeader("X-User-Email") String email,
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody ExpenseRequest request
    );
}
