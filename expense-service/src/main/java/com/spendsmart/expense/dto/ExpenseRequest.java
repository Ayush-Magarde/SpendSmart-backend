package com.spendsmart.expense.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseRequest {

    private Double amount;
    private String category; // Changed from enum to string for frontend compatibility
    private String description;
    private String paymentMethod;
    private LocalDate date;
}