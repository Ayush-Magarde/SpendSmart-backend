package com.spendsmart.expense.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // Foreign key to User

    private Double amount;

    private String category; // Changed from enum to string for frontend compatibility

    private String description;

    private String paymentMethod;

    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
