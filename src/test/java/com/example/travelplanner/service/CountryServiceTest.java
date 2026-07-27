package com.example.travelplanner.service;

import com.example.travelplanner.model.Country;
import com.example.travelplanner.repository.CountryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CountryServiceTest {

    @Mock
    private CountryRepository countryRepository;

    private CountryService countryService;

    @BeforeEach
    void setUp() {
        countryService = new CountryService(countryRepository, new ObjectMapper());
    }

    @Test
    void seedCountriesReadsJsonAndSavesToDatabase() {
        when(countryRepository.saveAll(any())).thenAnswer(inv -> inv.getArgument(0));
        when(countryRepository.findAll()).thenReturn(List.of(
                new Country("Poland", "PLN", "Polish Zloty", "Złoty polski")
        ));

        List<Country> result = countryService.seedCountries();

        verify(countryRepository).deleteAll();
        verify(countryRepository).saveAll(any());
        assertFalse(result.isEmpty());
    }

    @Test
    void seedCountriesHandlesFailureGracefully() {
        doThrow(new RuntimeException("DB error")).when(countryRepository).deleteAll();
        when(countryRepository.findAll()).thenReturn(List.of());

        List<Country> result = countryService.seedCountries();

        assertTrue(result.isEmpty());
    }

    @Test
    void getAllCountriesReturnsFromRepository() {
        List<Country> expected = List.of(
                new Country("Poland", "PLN", "Polish Zloty", "Złoty polski"),
                new Country("Germany", "EUR", "Euro", "Euro")
        );
        when(countryRepository.findAll()).thenReturn(expected);

        List<Country> result = countryService.getAllCountries();

        assertEquals(2, result.size());
        assertEquals("Poland", result.get(0).getCountry());
        assertEquals("EUR", result.get(1).getCurrencyCode());
    }
}
