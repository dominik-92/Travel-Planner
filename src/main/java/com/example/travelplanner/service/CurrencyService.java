package com.example.travelplanner.service;

import com.example.travelplanner.model.Currency;
import com.example.travelplanner.repository.CurrencyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CurrencyService {

    private static final Logger log = LoggerFactory.getLogger(CurrencyService.class);
    private static final String NBP_TABLE_URL = "https://api.nbp.pl/api/exchangerates/tables/a/?format=json";
    private static final String NBP_RATE_URL = "https://api.nbp.pl/api/exchangerates/rates/a/%s/%s/%s/?format=json";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ISO_LOCAL_DATE;

    private static final Map<String, String> POLISH_TO_ENGLISH = Map.ofEntries(
            Map.entry("bat (Tajlandia)", "Thai Baht"),
            Map.entry("dolar amerykański", "US Dollar"),
            Map.entry("dolar australijski", "Australian Dollar"),
            Map.entry("dolar Hongkongu", "Hong Kong Dollar"),
            Map.entry("dolar kanadyjski", "Canadian Dollar"),
            Map.entry("dolar nowozelandzki", "New Zealand Dollar"),
            Map.entry("dolar singapurski", "Singapore Dollar"),
            Map.entry("euro", "Euro"),
            Map.entry("forint (Węgry)", "Hungarian Forint"),
            Map.entry("frank szwajcarski", "Swiss Franc"),
            Map.entry("funt szterling", "British Pound"),
            Map.entry("hrywna (Ukraina)", "Ukrainian Hryvnia"),
            Map.entry("jen (Japonia)", "Japanese Yen"),
            Map.entry("korona czeska", "Czech Koruna"),
            Map.entry("korona duńska", "Danish Krone"),
            Map.entry("korona islandzka", "Icelandic Króna"),
            Map.entry("korona norweska", "Norwegian Krone"),
            Map.entry("korona szwedzka", "Swedish Krona"),
            Map.entry("lej rumuński", "Romanian Leu"),
            Map.entry("lira turecka", "Turkish Lira"),
            Map.entry("nowy izraelski szekel", "Israeli New Shekel"),
            Map.entry("peso chilijskie", "Chilean Peso"),
            Map.entry("peso filipińskie", "Philippine Peso"),
            Map.entry("peso meksykańskie", "Mexican Peso"),
            Map.entry("rand (Republika Południowej Afryki)", "South African Rand"),
            Map.entry("real (Brazylia)", "Brazilian Real"),
            Map.entry("ringgit (Malezja)", "Malaysian Ringgit"),
            Map.entry("rupia indonezyjska", "Indonesian Rupiah"),
            Map.entry("rupia indyjska", "Indian Rupee"),
            Map.entry("won południowokoreański", "South Korean Won"),
            Map.entry("yuan renminbi (Chiny)", "Chinese Yuan"),
            Map.entry("SDR (MFW)", "SDR (IMF)")
    );

    private final CurrencyRepository currencyRepository;
    private final RestTemplate restTemplate;

    public CurrencyService(CurrencyRepository currencyRepository, RestTemplate restTemplate) {
        this.currencyRepository = currencyRepository;
        this.restTemplate = restTemplate;
    }

    @SuppressWarnings("unchecked")
    public List<Currency> fetchAndSaveCurrencies() {
        try {
            List<Map<String, Object>> response = restTemplate.getForObject(NBP_TABLE_URL, List.class);
            if (response == null || response.isEmpty()) {
                log.warn("Empty response from NBP API");
                return currencyRepository.findAll();
            }

            Map<String, Object> table = response.getFirst();
            String effectiveDate = (String) table.get("effectiveDate");
            List<Map<String, Object>> rates = (List<Map<String, Object>>) table.get("rates");

            Currency pln = new Currency("PLN", "Polish Złoty", 1.0, effectiveDate);
            currencyRepository.save(pln);

            for (Map<String, Object> rateEntry : rates) {
                String code = (String) rateEntry.get("code");
                String polishName = (String) rateEntry.get("currency");
                double mid = ((Number) rateEntry.get("mid")).doubleValue();
                String englishName = POLISH_TO_ENGLISH.getOrDefault(polishName, polishName);
                currencyRepository.save(new Currency(code, englishName, mid, effectiveDate));
            }

            log.info("Saved {} currencies from NBP (effective date: {})", rates.size() + 1, effectiveDate);
            return currencyRepository.findAll();
        } catch (Exception e) {
            log.error("Failed to fetch currencies from NBP API: {}", e.getMessage());
            return currencyRepository.findAll();
        }
    }

    @Transactional(readOnly = true)
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    @Transactional(readOnly = true)
    public double getLatestRate(String code) {
        if ("PLN".equalsIgnoreCase(code)) {
            return 1.0;
        }
        return currencyRepository.findById(code.toUpperCase())
                .map(Currency::getRate)
                .orElse(1.0);
    }

    @SuppressWarnings("unchecked")
    public double getHistoricalRate(String code, String date) {
        if ("PLN".equalsIgnoreCase(code)) {
            return 1.0;
        }

        try {
            LocalDate expenseDate = LocalDate.parse(date.substring(0, 10), DATE_FMT);
            LocalDate startDate = expenseDate.minusDays(7);

            String url = String.format(NBP_RATE_URL, code.toLowerCase(), startDate.format(DATE_FMT), expenseDate.format(DATE_FMT));
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response == null) {
                return getLatestRate(code);
            }

            List<Map<String, Object>> rates = (List<Map<String, Object>>) response.get("rates");
            if (rates == null || rates.isEmpty()) {
                return getLatestRate(code);
            }

            Map<String, Object> lastRate = rates.getLast();
            return ((Number) lastRate.get("mid")).doubleValue();
        } catch (Exception e) {
            log.warn("Failed to fetch historical rate for {} on {}: {}", code, date, e.getMessage());
            return getLatestRate(code);
        }
    }

}
