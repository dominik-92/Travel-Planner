package com.example.travelplanner.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryExpenseSummary(
        String category,
        double amount,
        double percentage,
        Double amountInReportingCurrency) {
}
