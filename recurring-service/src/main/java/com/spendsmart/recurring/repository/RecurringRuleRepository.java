package com.spendsmart.recurring.repository;

import com.spendsmart.recurring.entity.RecurringRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecurringRuleRepository extends JpaRepository<RecurringRule, Long> {

    List<RecurringRule> findByUserId(Long userId);

    List<RecurringRule> findByActive(boolean active);

    List<RecurringRule> findAll();
}
