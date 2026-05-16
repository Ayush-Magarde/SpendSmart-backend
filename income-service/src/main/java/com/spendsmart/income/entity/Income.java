package com.spendsmart.income.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Income {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // from JWT

    private Double amount;

    private String category; // Changed from source to category for Category Service integration

    private String description;

    private LocalDate date;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false, nullable = true)
    @Setter(AccessLevel.NONE)
    private LocalDateTime createdAt;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
