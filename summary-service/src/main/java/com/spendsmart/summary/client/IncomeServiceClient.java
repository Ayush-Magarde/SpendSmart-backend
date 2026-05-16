package com.spendsmart.summary.client;

import com.spendsmart.summary.dto.IncomeDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "income-service")
public interface IncomeServiceClient {
    
    @GetMapping("/api/incomes")
    List<IncomeDTO> getIncomes(@RequestHeader("X-User-Email") String email, @RequestHeader("X-User-Id") Long userId);
}
