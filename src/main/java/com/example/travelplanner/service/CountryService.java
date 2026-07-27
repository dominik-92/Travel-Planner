package com.example.travelplanner.service;

import com.example.travelplanner.model.Country;
import com.example.travelplanner.repository.CountryRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

@Service
@Transactional
public class CountryService {

    private static final Logger log = LoggerFactory.getLogger(CountryService.class);

    private final CountryRepository countryRepository;
    private final ObjectMapper objectMapper;

    public CountryService(CountryRepository countryRepository, ObjectMapper objectMapper) {
        this.countryRepository = countryRepository;
        this.objectMapper = objectMapper;
    }

    public List<Country> seedCountries() {
        try {
            InputStream is = new ClassPathResource("countries.json").getInputStream();
            List<Country> countries = objectMapper.readValue(is, new TypeReference<>() {});

            countryRepository.deleteAll();
            countryRepository.saveAll(countries);

            log.info("Seeded {} countries from countries.json", countries.size());
            return countryRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to seed countries from JSON: {}", e.getMessage());
            return countryRepository.findAll();
        }
    }

    @Transactional(readOnly = true)
    public List<Country> getAllCountries() {
        return countryRepository.findAll();
    }
}
