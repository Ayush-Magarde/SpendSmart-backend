package com.spendsmart.recurring.service;

import com.spendsmart.recurring.dto.DueDateReminderRequest;
import com.spendsmart.recurring.entity.DueDateReminder;
import com.spendsmart.recurring.exception.RecurringException;
import com.spendsmart.recurring.repository.DueDateReminderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DueDateReminderServiceImpl implements DueDateReminderService {

    private final DueDateReminderRepository dueDateReminderRepository;

    @Override
    @Transactional
    public DueDateReminder addDueDateReminder(String userEmail, DueDateReminderRequest request) {
        log.info("Adding due date reminder for user: {}", userEmail);

        DueDateReminder reminder = new DueDateReminder();
        reminder.setUserEmail(userEmail);
        reminder.setTitle(request.getTitle());
        reminder.setMessage(request.getMessage());
        reminder.setTargetType(request.getTargetType());
        reminder.setTargetId(request.getTargetId());
        reminder.setDueDate(request.getDueDate());
        reminder.setReminderDate(request.getReminderDate());
        reminder.setReminderTime(request.getReminderTime());
        reminder.setReminderType(request.getReminderType());
        reminder.setReminderDaysBefore(request.getReminderDaysBefore());
        reminder.setIsRecurring(request.getIsRecurring() != null ? request.getIsRecurring() : false);
        reminder.setFrequency(request.getFrequency());
        reminder.setSentCount(0);
        reminder.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);
        reminder.setStatus("PENDING");
        reminder.setMetadata(request.getMetadata());

        DueDateReminder savedReminder = dueDateReminderRepository.save(reminder);
        log.info("Due date reminder added successfully with ID: {}", savedReminder.getId());

        return savedReminder;
    }

    @Override
    public List<DueDateReminder> getDueDateReminders(String userEmail) {
        log.info("Retrieving due date reminders for user: {}", userEmail);
        return dueDateReminderRepository.findByUserEmail(userEmail);
    }

    @Override
    public List<DueDateReminder> getActiveDueDateReminders(String userEmail) {
        log.info("Retrieving active due date reminders for user: {}", userEmail);
        return dueDateReminderRepository.findByUserEmailAndIsActive(userEmail, true);
    }

    @Override
    @Transactional
    public void deleteDueDateReminder(Long id, String userEmail) {
        log.info("Deleting due date reminder {} for user: {}", id, userEmail);

        Optional<DueDateReminder> reminderOpt = dueDateReminderRepository.findById(id);
        if (reminderOpt.isEmpty()) {
            throw new RecurringException("Due date reminder not found with ID: " + id);
        }

        DueDateReminder reminder = reminderOpt.get();
        if (!reminder.getUserEmail().equals(userEmail)) {
            throw new RecurringException("Access denied: Due date reminder does not belong to user");
        }

        dueDateReminderRepository.delete(reminder);
        log.info("Due date reminder deleted successfully with ID: {}", id);
    }

    @Override
    public List<DueDateReminder> getAllDueDateReminders() {
        log.info("Retrieving all due date reminders");
        return dueDateReminderRepository.findAll();
    }

    @Override
    public long getDueDateReminderCount() {
        log.info("Retrieving due date reminder count");
        return dueDateReminderRepository.count();
    }

    @Override
    @Transactional
    public DueDateReminder toggleDueDateReminder(Long id, String userEmail) {
        log.info("Toggling due date reminder {} for user: {}", id, userEmail);

        Optional<DueDateReminder> reminderOpt = dueDateReminderRepository.findById(id);
        if (reminderOpt.isEmpty()) {
            throw new RecurringException("Due date reminder not found with ID: " + id);
        }

        DueDateReminder reminder = reminderOpt.get();
        if (!reminder.getUserEmail().equals(userEmail)) {
            throw new RecurringException("Access denied: Due date reminder does not belong to user");
        }

        reminder.setIsActive(!reminder.getIsActive());
        DueDateReminder updatedReminder = dueDateReminderRepository.save(reminder);
        log.info("Due date reminder toggled successfully with ID: {}", id);

        return updatedReminder;
    }

    @Override
    public List<DueDateReminder> getDueDateRemindersByType(String userEmail, String targetType) {
        log.info("Retrieving due date reminders of type {} for user: {}", targetType, userEmail);
        return dueDateReminderRepository.findByUserEmailAndTargetType(userEmail, targetType);
    }

    @Override
    public List<DueDateReminder> getPendingReminders() {
        log.info("Retrieving pending reminders");
        return dueDateReminderRepository.findByStatus("PENDING");
    }

    @Override
    @Transactional
    public DueDateReminder markReminderAsSent(Long id) {
        log.info("Marking reminder {} as sent", id);

        Optional<DueDateReminder> reminderOpt = dueDateReminderRepository.findById(id);
        if (reminderOpt.isEmpty()) {
            throw new RecurringException("Due date reminder not found with ID: " + id);
        }

        DueDateReminder reminder = reminderOpt.get();
        reminder.setStatus("SENT");
        reminder.setSentCount(reminder.getSentCount() + 1);
        reminder.setLastSentDate(java.time.LocalDate.now().toString());

        DueDateReminder updatedReminder = dueDateReminderRepository.save(reminder);
        log.info("Reminder marked as sent successfully with ID: {}", id);

        return updatedReminder;
    }

    @Override
    @Transactional
    public void markReminderAsFailed(Long id) {
        log.info("Marking reminder {} as failed", id);

        Optional<DueDateReminder> reminderOpt = dueDateReminderRepository.findById(id);
        if (reminderOpt.isEmpty()) {
            throw new RecurringException("Due date reminder not found with ID: " + id);
        }

        DueDateReminder reminder = reminderOpt.get();
        reminder.setStatus("FAILED");

        dueDateReminderRepository.save(reminder);
        log.info("Reminder marked as failed successfully with ID: {}", id);
    }

    @Override
    public List<DueDateReminder> getExpiredReminders(String cutoffDate) {
        log.info("Retrieving expired reminders before date: {}", cutoffDate);
        return dueDateReminderRepository.findByReminderDateBeforeAndStatusNot(cutoffDate, "EXPIRED");
    }

    @Override
    @Transactional
    public void markReminderAsExpired(Long id) {
        log.info("Marking reminder {} as expired", id);

        Optional<DueDateReminder> reminderOpt = dueDateReminderRepository.findById(id);
        if (reminderOpt.isEmpty()) {
            throw new RecurringException("Due date reminder not found with ID: " + id);
        }

        DueDateReminder reminder = reminderOpt.get();
        reminder.setStatus("EXPIRED");

        dueDateReminderRepository.save(reminder);
        log.info("Reminder marked as expired successfully with ID: {}", id);
    }

    @Override
    @Transactional
    public void updateReminderSchedule(DueDateReminder reminder) {
        log.info("Updating reminder schedule for ID: {}", reminder.getId());
        
        dueDateReminderRepository.save(reminder);
        log.info("Reminder schedule updated successfully for ID: {}", reminder.getId());
    }
}
