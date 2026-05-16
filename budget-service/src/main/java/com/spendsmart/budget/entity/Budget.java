package com.spendsmart.budget.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "budgets", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_category", columnNames = {"user_id", "category"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // from JWT

    private String name;

    private Double amount;

    private Double spent;

    private String category;

    private Double alertThreshold;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Column(name = "is_active")
    private Boolean active;
}
