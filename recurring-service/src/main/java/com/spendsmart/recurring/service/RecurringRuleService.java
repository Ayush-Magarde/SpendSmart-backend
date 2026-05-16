package com.spendsmart.recurring.service;

import com.spendsmart.recurring.dto.RecurringRuleRequest;
import com.spendsmart.recurring.entity.RecurringRule;

import java.util.List;

public interface RecurringRuleService {

    RecurringRule addRecurringRule(Long userId, String email, RecurringRuleRequest request);

    List<RecurringRule> getRecurringRules(Long userId);

    RecurringRule updateRecurringRule(Long id, Long userId, RecurringRuleRequest request);

    void deleteRecurringRule(Long id, Long userId);
}
