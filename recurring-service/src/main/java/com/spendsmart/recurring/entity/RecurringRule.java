package com.spendsmart.recurring.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "recurring_rules")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecurringRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // from JWT

    private String userEmail; // store real user email for background processing

    private String name;

    private String type; // EXPENSE, INCOME, PAYMENT

    private Double amount;

    private String frequency; // DAILY, WEEKLY, MONTHLY

    private LocalDateTime startDate;

    private LocalDateTime nextDate;

    private Boolean active;

    private LocalDateTime createdAt;
}
