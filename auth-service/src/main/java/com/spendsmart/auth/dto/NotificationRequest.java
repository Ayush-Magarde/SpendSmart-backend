package com.spendsmart.auth.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    private String title;
    private String message;
    private String type; // INFO, WARNING, ERROR, SUCCESS
    private String category; // BUDGET, PAYMENT, EXPENSE, SYSTEM
    private Boolean isRead;
}
