package com.spendsmart.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BudgetRequest {

    @NotBlank(message = "Budget name is required")
    private String name;

    @NotNull(message = "Budget amount is required")
    @Positive(message = "Budget amount must be positive")
    private Double amount;

    private String category;

    private Double alertThreshold;
}
