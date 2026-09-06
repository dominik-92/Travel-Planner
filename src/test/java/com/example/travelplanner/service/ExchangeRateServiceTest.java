package com.example.travelplanner.service;

import com.example.travelplanner.model.ExchangeRate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExchangeRateServiceTest {

    @Mock
    private NbpExchangeRateProvider nbpProvider;

    @Mock
    private FrankfurterExchangeRateProvider frankfurterProvider;

    @InjectMocks
    private ExchangeRateService exchangeRateService;

    @Test
    void resolveUsesNbpWhenTargetIsPln() {
        LocalDate date = LocalDate.of(2026, 8, 1);
        when(nbpProvider.findRate("EUR", "PLN", date))
                .thenReturn(Optional.of(new ExchangeRate("EUR", "PLN", 4.32, date, "NBP")));

        Optional<ExchangeRate> result = exchangeRateService.resolve("EUR", "PLN", date);

        assertTrue(result.isPresent());
        assertEquals("NBP", result.get().provider());
        assertEquals(4.32, result.get().rate(), 0.001);
        verify(frankfurterProvider, never()).findRate(anyString(), anyString(), any(LocalDate.class));
    }

    @Test
    void resolveUsesFrankfurterWhenTargetIsNotPln() {
        LocalDate date = LocalDate.of(2026, 8, 1);
        when(frankfurterProvider.findRate("EUR", "USD", date))
                .thenReturn(Optional.of(new ExchangeRate("EUR", "USD", 1.09, date, "FRANKFURTER")));

        Optional<ExchangeRate> result = exchangeRateService.resolve("EUR", "USD", date);

        assertTrue(result.isPresent());
        assertEquals("FRANKFURTER", result.get().provider());
        assertEquals(1.09, result.get().rate(), 0.001);
        verify(nbpProvider, never()).findRate(anyString(), anyString(), any(LocalDate.class));
    }

    @Test
    void resolveReturnsEmptyWhenProviderReturnsEmpty() {
        LocalDate date = LocalDate.of(2026, 8, 1);
        when(frankfurterProvider.findRate("EUR", "USD", date)).thenReturn(Optional.empty());

        Optional<ExchangeRate> result = exchangeRateService.resolve("EUR", "USD", date);

        assertTrue(result.isEmpty());
    }
}
