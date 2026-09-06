package com.example.travelplanner.model;

import java.time.LocalDate;

public record ExchangeRate(String from, String to, double rate, LocalDate date, String provider) {
}
