
package com.spendsmart.summary.service;

import com.spendsmart.summary.dto.SummaryResponse;

public interface SummaryService {
    SummaryResponse getSummary(String email, Long userId);
}