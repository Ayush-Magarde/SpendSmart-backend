package com.spendsmart.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "Title cannot be blank")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @NotBlank(message = "Message cannot be blank")
    @Size(max = 500, message = "Message cannot exceed 500 characters")
    private String message;

    @NotNull(message = "Type cannot be null")
    @Pattern(regexp = "^(INFO|WARNING|ERROR|SUCCESS)$", message = "Type must be one of: INFO, WARNING, ERROR, SUCCESS")
    private String type; // INFO, WARNING, ERROR, SUCCESS

    @NotNull(message = "Category cannot be null")
    @Pattern(regexp = "^(BUDGET|PAYMENT|EXPENSE|SYSTEM)$", message = "Category must be one of: BUDGET, PAYMENT, EXPENSE, SYSTEM")
    private String category; // BUDGET, PAYMENT, EXPENSE, SYSTEM

    private Boolean isRead;
}
