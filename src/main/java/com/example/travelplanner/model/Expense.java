package com.example.travelplanner.model;

public record Expense(
    String id,
    String category,
    double amount,
    String description,
    String addedAt
) {}