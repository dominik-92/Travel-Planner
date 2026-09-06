package com.example.travelplanner.service;

import com.example.travelplanner.model.ExchangeRate;

import java.time.LocalDate;
import java.util.Optional;

public interface ExchangeRateProvider {

    String name();

    Optional<ExchangeRate> findRate(String from, String to, LocalDate date);
}
