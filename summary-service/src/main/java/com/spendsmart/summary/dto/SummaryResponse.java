
package com.spendsmart.summary.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SummaryResponse {

    private Double totalIncome;
    private Double totalExpense;
    private Double balance;
    private Map<String, Double> categoryBreakdown;
    private List<Map<String, Object>> monthlyComparison;
}