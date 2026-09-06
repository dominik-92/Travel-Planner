package com.example.travelplanner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExpenseSummaryResponse(
        String currency,
        String reportingCurrency,
        double totalSpent,
        Double totalSpentInReportingCurrency,
        Boolean conversionApproximate,
        String conversionProvider,
        String conversionRateDate,
        List<CategoryExpenseSummary> categories) {
}
