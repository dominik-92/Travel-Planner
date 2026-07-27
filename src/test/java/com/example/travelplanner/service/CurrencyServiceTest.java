package com.example.travelplanner.service;

import com.example.travelplanner.model.Currency;
import com.example.travelplanner.repository.CurrencyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CurrencyServiceTest {

    @Mock
    private CurrencyRepository currencyRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CurrencyService currencyService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void fetchAndSaveCurrenciesSavesTranslatedCurrencies() {
        Map<String, Object> rateEntry = Map.of("currency", "euro", "code", "EUR", "mid", 4.3257);
        Map<String, Object> table = Map.of("effectiveDate", "2026-07-24", "rates", List.of(rateEntry));
        when(restTemplate.getForObject(contains("tables/a"), eq(List.class))).thenReturn(List.of(table));
        when(currencyRepository.save(any(Currency.class))).thenAnswer(inv -> inv.getArgument(0));
        when(currencyRepository.findAll()).thenReturn(List.of(new Currency("PLN", "Polish Złoty", 1.0, "2026-07-24"), new Currency("EUR", "Euro", 4.3257, "2026-07-24")));

        List<Currency> result = currencyService.fetchAndSaveCurrencies();

        verify(currencyRepository, atLeastOnce()).save(any(Currency.class));
        assertFalse(result.isEmpty());
    }

    @Test
    void fetchAndSaveCurrenciesHandlesApiFailure() {
        when(restTemplate.getForObject(anyString(), eq(List.class))).thenThrow(new RuntimeException("API error"));
        when(currencyRepository.findAll()).thenReturn(List.of());

        List<Currency> result = currencyService.fetchAndSaveCurrencies();

        assertTrue(result.isEmpty());
    }

    @Test
    void getLatestRateForPlnReturnsOne() {
        double rate = currencyService.getLatestRate("PLN");

        assertEquals(1.0, rate);
    }

    @Test
    void getLatestRateForEurReturnsDbRate() {
        when(currencyRepository.findById("EUR")).thenReturn(Optional.of(new Currency("EUR", "Euro", 4.3257, "2026-07-24")));

        double rate = currencyService.getLatestRate("EUR");

        assertEquals(4.3257, rate);
    }

    @Test
    void getLatestRateForUnknownCurrencyReturnsOne() {
        when(currencyRepository.findById("XYZ")).thenReturn(Optional.empty());

        double rate = currencyService.getLatestRate("XYZ");

        assertEquals(1.0, rate);
    }

    @Test
    void getHistoricalRateForPlnReturnsOne() {
        double rate = currencyService.getHistoricalRate("PLN", "2026-07-24T12:00:00Z");

        assertEquals(1.0, rate);
    }

    @Test
    void getHistoricalRateReturnsLastRateFromApiResponse() {
        Map<String, Object> rate1 = Map.of("effectiveDate", "2026-07-20", "mid", 4.3359);
        Map<String, Object> rate2 = Map.of("effectiveDate", "2026-07-24", "mid", 4.3257);
        Map<String, Object> response = Map.of("rates", List.of(rate1, rate2));
        when(restTemplate.getForObject(contains("rates/a/eur"), eq(Map.class))).thenReturn(response);

        double rate = currencyService.getHistoricalRate("EUR", "2026-07-26T12:00:00Z");

        assertEquals(4.3257, rate);
    }

    @Test
    void getHistoricalRateFallsBackToLatestOnApiFailure() {
        when(restTemplate.getForObject(contains("rates/a/eur"), eq(Map.class))).thenThrow(new RuntimeException("API error"));
        when(currencyRepository.findById("EUR")).thenReturn(Optional.of(new Currency("EUR", "Euro", 4.3257, "2026-07-24")));

        double rate = currencyService.getHistoricalRate("EUR", "2026-07-26T12:00:00Z");

        assertEquals(4.3257, rate);
    }

    @Test
    void getAllCurrenciesReturnsFromRepository() {
        List<Currency> expected = List.of(new Currency("PLN", "Polish Złoty", 1.0, "2026-07-24"));
        when(currencyRepository.findAll()).thenReturn(expected);

        List<Currency> result = currencyService.getAllCurrencies();

        assertEquals(1, result.size());
        assertEquals("PLN", result.get(0).getCode());
    }
}
