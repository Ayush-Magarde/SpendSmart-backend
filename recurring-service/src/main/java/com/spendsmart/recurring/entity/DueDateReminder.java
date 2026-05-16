package com.spendsmart.recurring.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "due_date_reminders")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DueDateReminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "User email is required")
    private String userEmail; // from JWT

    @NotBlank(message = "Reminder title is required")
    private String title;

    @NotBlank(message = "Reminder message is required")
    private String message;

    @NotBlank(message = "Target type is required")
    private String targetType; // BUDGET, EXPENSE, PAYMENT, BILL

    private Long targetId; // ID of the target entity

    @NotNull(message = "Due date is required")
    private String dueDate;

    @NotNull(message = "Reminder date is required")
    private String reminderDate;

    @NotNull(message = "Reminder time is required")
    private String reminderTime;

    private String status; // PENDING, SENT, FAILED, CANCELLED

    private String reminderType; // EMAIL, SMS, PUSH, IN_APP

    private Integer reminderDaysBefore; // Days before due date to send reminder

    private Boolean isRecurring; // Whether this is a recurring reminder

    private String frequency; // DAILY, WEEKLY, MONTHLY

    private Integer sentCount; // Number of times reminder was sent

    private String lastSentDate; // When reminder was last sent

    private String nextReminderDate; // When to send next reminder

    private Boolean isActive;

    private String metadata; // JSON string for additional data
}
