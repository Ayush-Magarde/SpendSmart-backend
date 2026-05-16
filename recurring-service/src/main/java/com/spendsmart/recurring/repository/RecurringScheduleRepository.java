package com.spendsmart.recurring.repository;

import com.spendsmart.recurring.entity.RecurringSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringScheduleRepository extends JpaRepository<RecurringSchedule, Long> {

    List<RecurringSchedule> findByUserEmail(String userEmail);

    List<RecurringSchedule> findByUserEmailAndStatus(String userEmail, String status);

    List<RecurringSchedule> findByRecurringRuleId(Long recurringRuleId);

    List<RecurringSchedule> findByStatus(String status);

    List<RecurringSchedule> findAll();
}
