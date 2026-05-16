package com.spendsmart.budget.client;

import com.spendsmart.budget.dto.ExpenseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "expense-service")
public interface ExpenseServiceClient {

    @GetMapping("/api/expenses")
    List<ExpenseDTO> getExpenses(
            @RequestHeader("X-User-Email") String email,
            @RequestHeader("X-User-Id") Long userId
    );
}
