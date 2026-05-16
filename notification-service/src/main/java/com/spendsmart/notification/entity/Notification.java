package com.spendsmart.notification.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId; // from JWT

    private String title;

    private String message;

    private String type; // INFO, WARNING, ERROR, SUCCESS

    private String category; // BUDGET, PAYMENT, EXPENSE, SYSTEM

    private LocalDateTime createdAt;

    private Boolean isRead;
}
