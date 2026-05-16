package com.spendsmart.recurring.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class IncomeRequest {

    private Double amount;
    private String category; // Changed from source to category for Category Service integration
    private String description;
    private LocalDate date;
}
