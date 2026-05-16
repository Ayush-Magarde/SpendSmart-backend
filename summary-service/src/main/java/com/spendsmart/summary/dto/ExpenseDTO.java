package com.spendsmart.summary.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseDTO {
    private Long id;
    private Long userId;
    private Double amount;
    private String category;
    private String description;
    private String paymentMethod;
    private LocalDate date;
}
