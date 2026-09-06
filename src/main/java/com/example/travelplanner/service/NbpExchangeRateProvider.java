package com.example.travelplanner.service;

import com.example.travelplanner.model.ExchangeRate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class NbpExchangeRateProvider implements ExchangeRateProvider {

    private static final String NBP_RATE_URL =
            "https://api.nbp.pl/api/exchangerates/rates/a/%s/%s/%s/?format=json";

    private final RestTemplate restTemplate;

    public NbpExchangeRateProvider(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String name() {
        return "NBP";
    }

    @Override
    public Optional<ExchangeRate> findRate(String from, String to, LocalDate date) {
        if (!"PLN".equalsIgnoreCase(to)) {
            return Optional.empty();
        }
        if ("PLN".equalsIgnoreCase(from)) {
            return Optional.of(new ExchangeRate(from, to, 1.0, date, name()));
        }
        try {
            LocalDate start = date.minusDays(7);
            String url = String.format(NBP_RATE_URL, from.toLowerCase(), start, date);
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);
            if (response == null) {
                return Optional.empty();
            }
            List<Map<String, Object>> rates = (List<Map<String, Object>>) response.get("rates");
            if (rates == null || rates.isEmpty()) {
                return Optional.empty();
            }
            Map<String, Object> last = rates.get(rates.size() - 1);
            double mid = ((Number) last.get("mid")).doubleValue();
            LocalDate effectiveDate = LocalDate.parse((String) last.get("effectiveDate"));
            return Optional.of(new ExchangeRate(from, to, mid, effectiveDate, name()));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
