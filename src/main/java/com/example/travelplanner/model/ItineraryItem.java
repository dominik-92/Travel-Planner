package com.example.travelplanner.model;

public record ItineraryItem(
    String id,
    int day,
    String time,
    String title,
    String description
) {}