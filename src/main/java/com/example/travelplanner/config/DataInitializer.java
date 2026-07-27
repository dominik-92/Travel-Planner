package com.example.travelplanner.config;

import com.example.travelplanner.service.CountryService;
import com.example.travelplanner.service.CurrencyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CurrencyService currencyService;
    private final CountryService countryService;

    public DataInitializer(CurrencyService currencyService, CountryService countryService) {
        this.currencyService = currencyService;
        this.countryService = countryService;
    }

    @Override
    public void run(String... args) {
        log.info("Seeding countries from countries.json...");
        countryService.seedCountries();

        if (currencyService.getAllCurrencies().isEmpty()) {
            log.info("Currencies table is empty. Fetching from NBP API...");
            currencyService.fetchAndSaveCurrencies();
        } else {
            log.info("Currencies table already populated. Skipping NBP fetch.");
        }
    }
}
