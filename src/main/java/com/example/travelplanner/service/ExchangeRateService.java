package com.example.travelplanner.service;

import com.example.travelplanner.model.ExchangeRate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class ExchangeRateService {

    private final NbpExchangeRateProvider nbpProvider;
    private final FrankfurterExchangeRateProvider frankfurterProvider;

    public ExchangeRateService(NbpExchangeRateProvider nbpProvider,
                               FrankfurterExchangeRateProvider frankfurterProvider) {
        this.nbpProvider = nbpProvider;
        this.frankfurterProvider = frankfurterProvider;
    }

    public Optional<ExchangeRate> resolve(String from, String to, LocalDate date) {
        ExchangeRateProvider provider = selectProvider(to);
        if (provider == null) {
            return Optional.empty();
        }
        return provider.findRate(from, to, date);
    }

    private ExchangeRateProvider selectProvider(String targetCurrency) {
        if ("PLN".equalsIgnoreCase(targetCurrency)) {
            return nbpProvider;
        }
        return frankfurterProvider;
    }
}
