package com.spendsmart.recurring.repository;

import com.spendsmart.recurring.entity.DueDateReminder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DueDateReminderRepository extends JpaRepository<DueDateReminder, Long> {

    List<DueDateReminder> findByUserEmail(String userEmail);

    List<DueDateReminder> findByUserEmailAndIsActive(String userEmail, Boolean isActive);

    List<DueDateReminder> findByUserEmailAndTargetType(String userEmail, String targetType);

    List<DueDateReminder> findByStatus(String status);

    List<DueDateReminder> findByReminderDate(String reminderDate);

    List<DueDateReminder> findAll();

    List<DueDateReminder> findByReminderDateBeforeAndStatusNot(String cutoffDate, String status);
}
