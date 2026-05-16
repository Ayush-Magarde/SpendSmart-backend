package com.spendsmart.recurring.service;

import com.spendsmart.recurring.dto.DueDateReminderRequest;
import com.spendsmart.recurring.entity.DueDateReminder;

import java.util.List;

public interface DueDateReminderService {

    DueDateReminder addDueDateReminder(String userEmail, DueDateReminderRequest request);

    List<DueDateReminder> getDueDateReminders(String userEmail);

    List<DueDateReminder> getActiveDueDateReminders(String userEmail);

    void deleteDueDateReminder(Long id, String userEmail);

    List<DueDateReminder> getAllDueDateReminders();

    long getDueDateReminderCount();

    DueDateReminder toggleDueDateReminder(Long id, String userEmail);

    List<DueDateReminder> getDueDateRemindersByType(String userEmail, String targetType);

    List<DueDateReminder> getPendingReminders();

    DueDateReminder markReminderAsSent(Long id);

    void markReminderAsFailed(Long id);

    List<DueDateReminder> getExpiredReminders(String cutoffDate);

    void markReminderAsExpired(Long id);

    void updateReminderSchedule(DueDateReminder reminder);
}
