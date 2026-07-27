package com.example.travelplanner.controller;

import com.example.travelplanner.model.Currency;
import com.example.travelplanner.service.CurrencyService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/currencies")
public class CurrencyController {

    private final CurrencyService currencyService;

    public CurrencyController(CurrencyService currencyService) {
        this.currencyService = currencyService;
    }

    @GetMapping
    public List<Currency> getAllCurrencies() {
        return currencyService.getAllCurrencies();
    }

    @GetMapping("/rate/{code}")
    public Map<String, Object> getRate(@PathVariable String code) {
        double rate = currencyService.getLatestRate(code);
        return Map.of("code", code.toUpperCase(), "rate", rate);
    }

    @PostMapping("/refresh")
    public List<Currency> refreshCurrencies() {
        return currencyService.fetchAndSaveCurrencies();
    }
}
