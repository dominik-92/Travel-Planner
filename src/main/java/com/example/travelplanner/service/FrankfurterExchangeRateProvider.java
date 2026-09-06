package com.example.travelplanner.service;

import com.example.travelplanner.model.ExchangeRate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class FrankfurterExchangeRateProvider implements ExchangeRateProvider {

    private static final String FRANKFURTER_RATES_URL =
            "https://api.frankfurter.dev/v2/rates?base=%s&quotes=%s&date=%s";

    private final RestTemplate restTemplate;

    public FrankfurterExchangeRateProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String name() {
        return "FRANKFURTER";
    }

    @Override
    public Optional<ExchangeRate> findRate(String from, String to, LocalDate date) {
        try {
            String url = String.format(FRANKFURTER_RATES_URL, from.toUpperCase(), to.toUpperCase(), date);
            List<Map<String, Object>> response = restTemplate.getForObject(url, List.class);
            if (response == null || response.isEmpty()) {
                return Optional.empty();
            }
            Map<String, Object> first = response.get(0);
            double rate = ((Number) first.get("rate")).doubleValue();
            String rateDate = (String) first.get("date");
            LocalDate effective = rateDate != null ? LocalDate.parse(rateDate) : date;
            return Optional.of(new ExchangeRate(from, to, rate, effective, name()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
