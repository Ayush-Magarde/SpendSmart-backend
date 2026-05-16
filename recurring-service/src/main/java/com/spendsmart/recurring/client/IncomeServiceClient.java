package com.spendsmart.recurring.client;

import com.spendsmart.recurring.dto.IncomeRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "income-service")
public interface IncomeServiceClient {
    
    @PostMapping("/api/incomes")
    void createIncome(
        @RequestHeader("X-User-Email") String email,
        @RequestHeader("X-User-Id") Long userId,
        @RequestBody IncomeRequest request
    );
}
